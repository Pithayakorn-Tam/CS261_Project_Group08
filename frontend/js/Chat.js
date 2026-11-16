const isLocalLivePreview =
    window.location.protocol === "file:" ||
    window.location.port === "5500" ||
    window.location.port === "5501";

const API_BASE_URL = isLocalLivePreview ? "http://localhost:8080/api" : "/api";
const WS_PROTOCOL = window.location.protocol === "https:" ? "wss:" : "ws:";
const WS_URL = isLocalLivePreview
    ? "ws://localhost:8080/ws/chat"
    : `${WS_PROTOCOL}//${window.location.host}/ws/chat`;

const MAX_MESSAGE_LENGTH = 2000;

const categoryLogos = {
    academic: "../Picture/book-one.svg",
    study: "../Picture/book-one.svg",
    "วิชาการ": "../Picture/book-one.svg",
    sport: "../Picture/activity.svg",
    "กีฬา": "../Picture/activity.svg",
    entertainment: "../Picture/gamecontroller.svg",
    "e-sport": "../Picture/gamecontroller.svg",
    concert: "../Picture/gamecontroller.svg",
    bordgame: "../Picture/gamecontroller.svg",
    boardgame: "../Picture/gamecontroller.svg",
    "บันเทิง": "../Picture/gamecontroller.svg",
    "life-style": "../Picture/bowl.svg",
    lifestyle: "../Picture/bowl.svg",
    food: "../Picture/bowl.svg",
    "ไลฟ์สไตล์": "../Picture/bowl.svg",
    other: "../Picture/bowl.svg",
    default: "../Picture/bowl.svg"
};

const badgeLogos = {
    "newly bee": "../Picture/badge/Newly-Bee.svg",
    "baby bloom": "../Picture/badge/Baby-Bloom.svg",
    "happy blossom": "../Picture/badge/Happy-Blossom.svg",
    "super bouquet": "../Picture/badge/Super-Bouquet.svg"
};

function getBadgeLogo(badgeName) {
    if (!badgeName) return null;
    const key = String(badgeName).trim().toLowerCase();
    return badgeLogos[key] || null;
}

function getCurrentStudentId() {
    for (const storage of [localStorage, sessionStorage]) {
        try {
            const rawLogin = storage.getItem("loginData");
            if (rawLogin) {
                const parsed = JSON.parse(rawLogin);
                if (parsed && (parsed.studentId || parsed.userId)) {
                    return String(parsed.studentId || parsed.userId).trim();
                }
            }
            const directId = storage.getItem("studentId");
            if (directId && directId.trim()) {
                return directId.trim();
            }
        } catch (err) {
            console.warn("Cannot parse stored loginData:", err);
        }
    }
    return "6800000000";
}

function getCategoryLogo(eventType) {
    if (!eventType) return categoryLogos.default;
    const key = String(eventType).trim().toLowerCase();
    return categoryLogos[key] || categoryLogos[String(eventType).trim()] || categoryLogos.default;
}

function normalizeRoomKey(roomIdOrEventId) {
    if (roomIdOrEventId == null) return "";
    const raw = String(roomIdOrEventId).trim();
    if (raw.toLowerCase().startsWith("room")) {
        return raw.slice(4).trim();
    }
    return raw;
}

function formatMessageTime(timestampStr) {
    if (!timestampStr) return "";
    const parsed = new Date(timestampStr);
    if (Number.isNaN(parsed.getTime())) return "";
    return parsed.toLocaleTimeString("th-TH", { hour: "2-digit", minute: "2-digit" });
}

const currentUserId = getCurrentStudentId();

let socket = null;
let currentRoom = null;
let rooms = [];
const messages = {};

// DOM
const chatListPage = document.getElementById("chatListPage");
const chatRoomPage = document.getElementById("chatRoomPage");
const chatList = document.getElementById("chatList");

const chatName = document.getElementById("chatName");
const chatRoomIcon = document.getElementById("chatRoomIcon");
const chatRoomCapacity = document.getElementById("chatRoomCapacity");
const messageArea = document.getElementById("messageArea");
const messageForm = document.getElementById("messageForm");
const messageInput = document.getElementById("messageInput");
const backBtn = document.getElementById("backBtn");

// เชื่อมต่อ WebSocket
function connectWebSocket() {
    if (
        socket &&
        (socket.readyState === WebSocket.OPEN ||
            socket.readyState === WebSocket.CONNECTING)
    ) {
        return;
    }

    try {
        socket = new WebSocket(WS_URL);
    } catch (error) {
        console.warn("WebSocket initialization failed:", error);
        return;
    }

    socket.addEventListener("open", () => {
        if (currentRoom) {
            joinRoom(currentRoom);
        }
    });

    socket.addEventListener("message", (event) => {
        try {
            const data = JSON.parse(event.data);
            receiveMessage(data);
        } catch (error) {
            console.error("Invalid WebSocket message:", error);
        }
    });

    socket.addEventListener("close", () => {
        socket = null;
    });

    socket.addEventListener("error", (error) => {
        console.warn("WebSocket Error:", error);
    });
}

// เข้าร่วมห้องบน WebSocket
function joinRoom(roomId) {
    if (!socket || socket.readyState !== WebSocket.OPEN || !roomId) return;

    socket.send(
        JSON.stringify({
            type: "join",
            roomId: String(roomId),
            userId: currentUserId
        })
    );
}

// ดึงรายการห้องแชทจากกิจกรรมที่ผู้ใช้เคยอยู่หรือกำลังอยู่
async function loadChatRooms() {
    try {
        const res = await fetch(
            `${API_BASE_URL}/chat/rooms?studentId=${encodeURIComponent(currentUserId)}`
        );
        const payload = await res.json();
        if (res.ok && payload && payload.status && Array.isArray(payload.data)) {
            rooms = payload.data;
            renderChatList();
            return;
        }
    } catch (err) {
        console.warn("Failed to load /api/chat/rooms, attempting event fallback:", err);
    }

    // Fallback กรณีเรียก /api/chat/rooms ไม่สำเร็จ ให้ประกอบรายการจากกิจกรรมที่สร้างและเข้าร่วม
    try {
        const [createdRes, joinedRes] = await Promise.all([
            fetch(`${API_BASE_URL}/events/user/${encodeURIComponent(currentUserId)}`),
            fetch(`${API_BASE_URL}/events/participant/${encodeURIComponent(currentUserId)}`)
        ]);
        const createdPayload = createdRes.ok ? await createdRes.json() : { data: [] };
        const joinedPayload = joinedRes.ok ? await joinedRes.json() : { data: [] };

        const uniqueMap = new Map();
        for (const list of [createdPayload.data || [], joinedPayload.data || []]) {
            for (const ev of list) {
                if (!ev || ev.id == null) continue;
                const status = String(ev.lifecycleStatus || ev.status || "").toUpperCase();
                if (status === "CANCELLED") continue;
                if (!uniqueMap.has(String(ev.id))) {
                    uniqueMap.set(String(ev.id), {
                        eventId: ev.id,
                        eventName: ev.eventName || "กิจกรรม",
                        eventType: ev.eventType || "other",
                        currentParticipants: ev.currentParticipants || 1,
                        eventSize: ev.eventSize || 1,
                        lifecycleStatus: ev.lifecycleStatus || "UPCOMING",
                        lastMessage: null,
                        lastMessageTime: null
                    });
                }
            }
        }
        rooms = Array.from(uniqueMap.values()).sort((a, b) => {
            const aUpcoming = String(a.lifecycleStatus).toUpperCase() === "UPCOMING";
            const bUpcoming = String(b.lifecycleStatus).toUpperCase() === "UPCOMING";
            if (aUpcoming !== bUpcoming) return aUpcoming ? -1 : 1;
            return 0;
        });
        renderChatList();
    } catch (fallbackErr) {
        console.error("Failed to load chat rooms:", fallbackErr);
        renderChatListError("ไม่สามารถโหลดรายการห้องแชทได้ในขณะนี้");
    }
}

function renderChatListError(message) {
    chatList.innerHTML = "";
    const p = document.createElement("p");
    p.className = "chat-empty-state";
    p.textContent = message;
    chatList.appendChild(p);
}

function renderChatList() {
    chatList.innerHTML = "";

    if (!rooms || rooms.length === 0) {
        renderChatListError("ยังไม่มีห้องแชทกิจกรรมที่คุณเข้าร่วมหรือเคยเข้าร่วม");
        return;
    }

    rooms.forEach((room) => {
        const eventIdStr = String(room.eventId);

        const button = document.createElement("button");
        button.type = "button";
        button.className = "chat-item";
        button.dataset.room = eventIdStr;
        button.dataset.name = room.eventName || "กิจกรรม";

        const icon = document.createElement("img");
        icon.className = "chat-item-icon";
        icon.src = getCategoryLogo(room.eventType);
        icon.alt = room.eventType || "Activity";

        const titleSpan = document.createElement("span");
        titleSpan.className = "chat-item-title";
        titleSpan.textContent = room.eventName || "กิจกรรม";

        const capacitySpan = document.createElement("span");
        capacitySpan.className = "chat-item-capacity";
        const curCount = room.currentParticipants != null ? room.currentParticipants : 1;
        const maxCount = room.eventSize != null ? room.eventSize : curCount;
        capacitySpan.textContent = `${curCount}/${maxCount}`;

        button.appendChild(icon);
        button.appendChild(titleSpan);
        button.appendChild(capacitySpan);

        chatList.appendChild(button);
    });
}

// กดเลือกรายชื่อแชท
chatList.addEventListener("click", (event) => {
    const item = event.target.closest(".chat-item");
    if (!item) return;

    const eventId = normalizeRoomKey(item.dataset.room);
    if (!eventId) return;

    openChatRoom(eventId);
});

async function openChatRoom(eventId) {
    currentRoom = String(eventId);
    const roomObj = rooms.find((r) => String(r.eventId) === currentRoom);

    chatName.textContent = roomObj?.eventName || "กิจกรรม";
    if (chatRoomIcon) {
        chatRoomIcon.src = getCategoryLogo(roomObj?.eventType);
    }
    if (chatRoomCapacity) {
        const curCount = roomObj?.currentParticipants != null ? roomObj.currentParticipants : 1;
        const maxCount = roomObj?.eventSize != null ? roomObj.eventSize : curCount;
        chatRoomCapacity.textContent = `${curCount}/${maxCount}`;
    }

    chatListPage.hidden = true;
    chatRoomPage.hidden = false;

    connectWebSocket();
    if (socket && socket.readyState === WebSocket.OPEN) {
        joinRoom(currentRoom);
    }

    await loadRoomMessages(currentRoom);
}

async function loadRoomMessages(eventId) {
    const roomKey = String(eventId);
    if (!messages[roomKey]) {
        messageArea.innerHTML = "";
        const loading = document.createElement("div");
        loading.className = "chat-empty-state";
        loading.textContent = "กำลังโหลดประวัติข้อความ...";
        messageArea.appendChild(loading);
    } else {
        renderMessages();
    }

    try {
        const res = await fetch(
            `${API_BASE_URL}/chat/events/${encodeURIComponent(roomKey)}/messages?studentId=${encodeURIComponent(currentUserId)}`
        );
        const payload = await res.json();

        if (!res.ok || !payload || !payload.status) {
            messages[roomKey] = [];
            renderChatRoomError(payload?.message || "ไม่สามารถโหลดประวัติข้อความได้");
            return;
        }

        messages[roomKey] = Array.isArray(payload.data) ? payload.data : [];
        if (currentRoom === roomKey) {
            renderMessages();
        }
    } catch (error) {
        console.error("Failed to fetch chat history:", error);
        if (!messages[roomKey] || messages[roomKey].length === 0) {
            renderChatRoomError("ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์เพื่อโหลดประวัติข้อความได้");
        }
    }
}

function renderChatRoomError(errorText) {
    messageArea.innerHTML = "";
    const empty = document.createElement("div");
    empty.className = "chat-empty-state";
    empty.textContent = errorText;
    messageArea.appendChild(empty);
}

// ย้อนกลับหน้ารายชื่อ
backBtn.addEventListener("click", () => {
    chatRoomPage.hidden = true;
    chatListPage.hidden = false;
    currentRoom = null;
    loadChatRooms();
});

// ส่งข้อความผ่าน REST API และให้ Backend บันทึก + Broadcast ผ่าน WebSocket
messageForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const text = messageInput.value.trim();
    if (!text || !currentRoom) return;

    if (text.length > MAX_MESSAGE_LENGTH) {
        alert(`ข้อความยาวเกินกำหนด (สูงสุด ${MAX_MESSAGE_LENGTH} ตัวอักษร)`);
        return;
    }

    const submitBtn = messageForm.querySelector('button[type="submit"]');
    if (submitBtn) submitBtn.disabled = true;

    try {
        const res = await fetch(
            `${API_BASE_URL}/chat/events/${encodeURIComponent(currentRoom)}/messages`,
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    senderStudentId: currentUserId,
                    text: text
                })
            }
        );

        const payload = await res.json();
        if (!res.ok || !payload || !payload.status || !payload.data) {
            alert(payload?.message || "ไม่สามารถส่งข้อความได้");
            return;
        }

        appendOrUpdateMessage(payload.data);
        messageInput.value = "";
        messageInput.focus();
    } catch (error) {
        console.error("Send message failed:", error);
        alert("เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์ ไม่สามารถส่งข้อความได้");
    } finally {
        if (submitBtn) submitBtn.disabled = false;
    }
});

// รับข้อความจาก Server ผ่าน WebSocket
function receiveMessage(data) {
    if (!data || data.type !== "message") return;
    appendOrUpdateMessage(data);
}

function appendOrUpdateMessage(data) {
    const roomKey = normalizeRoomKey(data.eventId != null ? data.eventId : data.roomId);
    if (!roomKey) return;

    if (!messages[roomKey]) {
        messages[roomKey] = [];
    }

    const isDuplicate =
        data.id != null &&
        messages[roomKey].some(
            (existing) => existing.id != null && String(existing.id) === String(data.id)
        );

    if (!isDuplicate) {
        messages[roomKey].push(data);
    }

    // อัปเดตข้อความล่าสุดในการ์ดรายการห้องแชท
    const roomObj = rooms.find((r) => String(r.eventId) === roomKey);
    if (roomObj && data.text) {
        roomObj.lastMessage = data.text;
        roomObj.lastMessageTime = data.timestamp || roomObj.lastMessageTime;
        if (chatRoomPage.hidden) {
            renderChatList();
        }
    }

    if (roomKey === currentRoom) {
        renderMessages();
    }
}

// แสดงข้อความตามลำดับเวลา
function renderMessages() {
    messageArea.innerHTML = "";

    const roomMessages = [...(messages[currentRoom] || [])].sort((a, b) => {
        const timeA = a.timestamp ? new Date(a.timestamp).getTime() : 0;
        const timeB = b.timestamp ? new Date(b.timestamp).getTime() : 0;
        if (timeA !== timeB && !Number.isNaN(timeA) && !Number.isNaN(timeB)) {
            return timeA - timeB;
        }
        if (a.id != null && b.id != null) {
            return Number(a.id) - Number(b.id);
        }
        return 0;
    });

    if (roomMessages.length === 0) {
        const empty = document.createElement("div");
        empty.className = "chat-empty-state";
        empty.textContent = "ยังไม่มีประวัติข้อความ เริ่มคุยกับเพื่อนในกิจกรรมนี้ได้เลย";
        messageArea.appendChild(empty);
        return;
    }

    roomMessages.forEach((message) => {
        const senderId = String(message.senderStudentId || message.senderId || "").trim();
        const isMine = senderId === String(currentUserId);

        const row = document.createElement("div");
        row.classList.add("message-row", isMine ? "sent" : "received");

        const avatar = document.createElement("div");
        avatar.className = "message-avatar";
        avatar.setAttribute("aria-hidden", "true");

        let avatarSrc = message.senderAvatar;
        if (!avatarSrc && isMine) {
            try {
                const stored = JSON.parse(localStorage.getItem("loginData") || "{}");
                if (stored.avatarUrl) avatarSrc = stored.avatarUrl;
            } catch (_) {}
        }
        if (avatarSrc) {
            const avatarImg = document.createElement("img");
            avatarImg.className = "message-avatar-img";
            avatarImg.src = avatarSrc;
            avatarImg.alt = "";
            avatar.appendChild(avatarImg);
        }

        const group = document.createElement("div");
        group.className = "message-group";

        const bubble = document.createElement("div");
        bubble.className = "message";

        const textEl = document.createElement("div");
        textEl.className = "message-text";
        textEl.textContent = message.text || "";
        bubble.appendChild(textEl);

        const meta = document.createElement("div");
        meta.className = "message-meta";

        if (!isMine) {
            const authorWrap = document.createElement("div");
            authorWrap.className = "message-author";

            const senderEl = document.createElement("span");
            senderEl.className = "message-sender";
            senderEl.textContent = message.senderName || "Username";
            authorWrap.appendChild(senderEl);

            const badgeSrc = getBadgeLogo(message.senderBadge);
            if (badgeSrc) {
                const badgeIcon = document.createElement("img");
                badgeIcon.className = "message-badge-icon";
                badgeIcon.src = badgeSrc;
                badgeIcon.alt = message.senderBadge;
                authorWrap.appendChild(badgeIcon);
            }

            meta.appendChild(authorWrap);
        }

        const timeLabel = formatMessageTime(message.timestamp);
        if (timeLabel) {
            const timeEl = document.createElement("span");
            timeEl.className = "message-time";
            timeEl.textContent = timeLabel;
            meta.appendChild(timeEl);
        }

        group.appendChild(bubble);
        if (meta.childElementCount > 0) {
            group.appendChild(meta);
        }

        if (isMine) {
            row.appendChild(group);
            row.appendChild(avatar);
        } else {
            row.appendChild(avatar);
            row.appendChild(group);
        }

        messageArea.appendChild(row);
    });

    // เลื่อนไปข้อความล่าสุด
    messageArea.scrollTop = messageArea.scrollHeight;
}

document.addEventListener("DOMContentLoaded", () => {
    connectWebSocket();
    loadChatRooms();
});
