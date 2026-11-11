
const WS_URL = "ws://localhost:3000";
const currentUserId = "user001";

let socket = null;
let currentRoom = null;

// DOM
const chatListPage = document.getElementById("chatListPage");
const chatRoomPage = document.getElementById("chatRoomPage");
const chatList = document.getElementById("chatList");

const chatName = document.getElementById("chatName");
const messageArea = document.getElementById("messageArea");
const messageForm = document.getElementById("messageForm");
const messageInput = document.getElementById("messageInput");
const backBtn = document.getElementById("backBtn");

// เก็บข้อความแต่ละห้อง
const messages = {
    room1: [],
    room2: []
};

// เชื่อมต่อ WebSocket
function connectWebSocket() {
    if (socket &&
        (socket.readyState === WebSocket.OPEN ||
         socket.readyState === WebSocket.CONNECTING)) {
        return;
    }

    socket = new WebSocket(WS_URL);

    socket.addEventListener("open", () => {
        console.log("WebSocket Connected");

        if (currentRoom) {
            joinRoom(currentRoom);
        }
    });

    socket.addEventListener("message", (event) => {
        try {
            const data = JSON.parse(event.data);
            receiveMessage(data);
        } catch (error) {
            console.error("Invalid message:", error);
        }
    });

    socket.addEventListener("close", () => {
        console.log("WebSocket Disconnected");
    });

    socket.addEventListener("error", (error) => {
        console.error("WebSocket Error:", error);
    });
}

// เข้าร่วมห้อง
function joinRoom(roomId) {
    if (!socket || socket.readyState !== WebSocket.OPEN) return;

    socket.send(JSON.stringify({
        type: "join",
        roomId: roomId,
        userId: currentUserId
    }));
}

// กดเลือกรายชื่อแชท
chatList.addEventListener("click", (event) => {
    const item = event.target.closest(".chat-item");
    if (!item) return;

    currentRoom = item.dataset.room;

    chatName.textContent = item.dataset.name;

    chatListPage.hidden = true;
    chatRoomPage.hidden = false;

    renderMessages();

    connectWebSocket();

    if (socket.readyState === WebSocket.OPEN) {
        joinRoom(currentRoom);
    }
});

// ย้อนกลับหน้ารายชื่อ
backBtn.addEventListener("click", () => {
    chatRoomPage.hidden = true;
    chatListPage.hidden = false;
    currentRoom = null;
});

// ส่งข้อความ
messageForm.addEventListener("submit", (event) => {
    event.preventDefault();

    const text = messageInput.value.trim();

    if (!text || !currentRoom) return;

    // ต้องเชื่อมต่อ WebSocket ก่อนจึงส่งข้อความได้
    if (!socket || socket.readyState !== WebSocket.OPEN) {
        alert("ยังไม่ได้เชื่อมต่อกับ Chat Server");
        return;
    }

    const message = {
        type: "message",
        roomId: currentRoom,
        senderId: currentUserId,
        text: text,
        timestamp: new Date().toISOString()
    };

    socket.send(JSON.stringify(message));

    messageInput.value = "";
});

// รับข้อความจาก Server
function receiveMessage(data) {
    if (data.type !== "message") return;

    if (!messages[data.roomId]) {
        messages[data.roomId] = [];
    }

    messages[data.roomId].push(data);

    // แสดงข้อความเฉพาะห้องที่กำลังเปิด
    if (data.roomId === currentRoom) {
        renderMessages();
    }
}

// แสดงข้อความ
function renderMessages() {
    messageArea.innerHTML = "";

    const roomMessages = messages[currentRoom] || [];

    roomMessages.forEach((message) => {
        const messageElement = document.createElement("div");

        messageElement.classList.add("message");

        messageElement.classList.add(
            message.senderId === currentUserId
                ? "sent"
                : "received"
        );

        messageElement.textContent = message.text;

        messageArea.appendChild(messageElement);
    });

    // เลื่อนไปข้อความล่าสุด
    messageArea.scrollTop = messageArea.scrollHeight;
}
