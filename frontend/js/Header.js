
function hasActiveLoginSession() {
    try {
        if (localStorage.getItem("tumeet_logged_out") === "true") {
            return false;
        }
        const stored = JSON.parse(localStorage.getItem("loginData") || "{}");
        if (stored && stored.studentId) return true;
        const fallbackId = localStorage.getItem("studentId");
        if (fallbackId && String(fallbackId).trim()) return true;
    } catch (_) {}
    return false;
}

function enforceAuthenticatedAccess() {
    const path = (window.location.pathname || "").toLowerCase();
    if (path.endsWith("login.html") || path.endsWith("eventdetail.html")) {
        return true;
    }
    if (!hasActiveLoginSession()) {
        window.location.replace("./Login.html");
        return false;
    }
    return true;
}

enforceAuthenticatedAccess();
window.addEventListener("pageshow", () => {
    enforceAuthenticatedAccess();
});

document.addEventListener("DOMContentLoaded", () => {
    if (!enforceAuthenticatedAccess()) return;

    const API_BASE_URL = (window.location.protocol === "file:" || window.location.port === "5500" || window.location.port === "5501")
        ? "http://localhost:8080/api"
        : "/api";

    const profileBtn = document.getElementById("profileBtn");
    const loggedInMenu = document.getElementById("loggedInMenu");
    const guestMenu = document.getElementById("guestMenu");
    const logoutBtn = document.getElementById("logoutBtn");
    const headerActions = document.querySelector(".header-actions");
    const rawBellLink = headerActions
        ? (document.getElementById("notificationBtn") || headerActions.querySelector('a[aria-label="Notifications"], button[aria-label="Notifications"]'))
        : null;

    function getHeaderStudentId() {
        try {
            const stored = JSON.parse(localStorage.getItem("loginData") || "{}");
            if (stored.studentId) return String(stored.studentId);
            const fallbackId = localStorage.getItem("studentId");
            if (fallbackId) return String(fallbackId);
        } catch (_) {}
        return "6800000000";
    }

    function applyHeaderProfileAvatar(avatarUrl) {
        if (!profileBtn) return;
        const img = profileBtn.querySelector("img");
        if (!img) return;
        if (avatarUrl) {
            img.src = avatarUrl;
            img.classList.add("profile-avatar-loaded");
        } else {
            img.src = "../Picture/profile.svg";
            img.classList.remove("profile-avatar-loaded");
        }
    }

    async function loadHeaderProfileAvatar() {
        try {
            const stored = JSON.parse(localStorage.getItem("loginData") || "{}");
            if (stored.avatarUrl) {
                applyHeaderProfileAvatar(stored.avatarUrl);
            }
            const studentId = getHeaderStudentId();
            if (!studentId) return;

            const res = await fetch(`${API_BASE_URL}/user/${encodeURIComponent(studentId)}`);
            if (!res.ok) return;
            const result = await res.json();
            const user = result && result.data;
            if (!user) return;

            if (user.profileImage) {
                applyHeaderProfileAvatar(user.profileImage);
                localStorage.setItem("loginData", JSON.stringify({
                    ...stored,
                    studentId: user.studentId || studentId,
                    avatarUrl: user.profileImage
                }));
            } else if (stored.avatarUrl) {
                applyHeaderProfileAvatar(null);
                const { avatarUrl, ...rest } = stored;
                localStorage.setItem("loginData", JSON.stringify(rest));
            }
        } catch (_) {
            // ignore network/storage errors in shared header
        }
    }

    loadHeaderProfileAvatar();

    // =====================================
    // Notification Bell & Popup (US-11)
    // =====================================
    let notificationPopup = null;
    let notificationListEl = null;
    let notificationUnreadDot = null;
    let isNotificationOpen = false;

    const BADGE_ICON_MAP = {
        "Newly Bee": "../Picture/badge/Newly-Bee.svg",
        "Baby Bloom": "../Picture/badge/Baby-Bloom.svg",
        "Happy Blossom": "../Picture/badge/Happy-Blossom.svg",
        "Super Bouquet": "../Picture/badge/Super-Bouquet.svg"
    };

    function createNotificationIcon(item) {
        const iconWrap = document.createElement("div");
        iconWrap.className = "notification-icon";

        const type = String(item.type || "").toUpperCase();
        if (type === "BADGE_UPGRADED") {
            const badgeKey = Object.keys(BADGE_ICON_MAP).find(
                (name) => name.toLowerCase() === String(item.badge || item.title || "").trim().toLowerCase()
            ) || "Newly Bee";
            const img = document.createElement("img");
            img.src = BADGE_ICON_MAP[badgeKey];
            img.alt = badgeKey;
            img.className = "notification-badge-img";
            iconWrap.appendChild(img);
            return iconWrap;
        }

        if (type === "LEAVE_SUCCESS" || type === "USER_LEFT" || type === "EVENT_CANCELLED" || type === "CANCEL_SUCCESS") {
            iconWrap.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="11" fill="#A40000"/><path d="M8.2 8.2L15.8 15.8M15.8 8.2L8.2 15.8" stroke="#FFFFFF" stroke-width="2.6" stroke-linecap="round"/></svg>`;
            return iconWrap;
        }

        if (type === "EVENT_REMINDER") {
            iconWrap.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="10" fill="none" stroke="#293FE8" stroke-width="2.2"/><path d="M12 6.8V12.4L15.8 14.6" fill="none" stroke="#293FE8" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>`;
            return iconWrap;
        }

        // JOIN_SUCCESS, USER_JOINED, EVENT_COMPLETED
        iconWrap.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="11" fill="#16B800"/><path d="M7.2 12.3L10.4 15.5L16.8 9.0" fill="none" stroke="#FFFFFF" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"/></svg>`;
        return iconWrap;
    }

    function renderNotificationItems(items) {
        if (!notificationListEl) return;
        notificationListEl.innerHTML = "";

        if (!items || items.length === 0) {
            const emptyEl = document.createElement("div");
            emptyEl.className = "notification-empty";
            emptyEl.textContent = "ยังไม่มีการแจ้งเตือน";
            notificationListEl.appendChild(emptyEl);
            return;
        }

        items.forEach((item) => {
            const row = document.createElement("div");
            row.className = "notification-item";
            if (!item.isRead) {
                row.classList.add("unread");
            }

            row.appendChild(createNotificationIcon(item));

            const textEl = document.createElement("div");
            textEl.className = "notification-text";
            textEl.textContent = item.message || item.title || "";
            row.appendChild(textEl);

            notificationListEl.appendChild(row);
        });
    }

    function setUnreadIndicator(unreadCount) {
        if (!notificationUnreadDot) return;
        const count = Number(unreadCount) || 0;
        notificationUnreadDot.hidden = count <= 0;
    }

    async function loadNotifications() {
        if (!rawBellLink) return;
        const studentId = getHeaderStudentId();
        try {
            const res = await fetch(`${API_BASE_URL}/notifications?studentId=${encodeURIComponent(studentId)}`);
            if (!res.ok) return;
            const result = await res.json();
            if (!result || !result.status || !result.data) return;

            renderNotificationItems(result.data.notifications || []);
            setUnreadIndicator(result.data.unreadCount);
        } catch (_) {
            // ignore network errors silently
        }
    }

    async function markAllNotificationsRead() {
        const studentId = getHeaderStudentId();
        try {
            const res = await fetch(`${API_BASE_URL}/notifications/read-all?studentId=${encodeURIComponent(studentId)}`, {
                method: "PUT"
            });
            if (res.ok) {
                setUnreadIndicator(0);
            }
        } catch (_) {}
    }

    function setupNotificationUI() {
        if (!rawBellLink || !headerActions) return;

        let wrapper = rawBellLink.closest(".notification-dropdown");
        if (!wrapper) {
            wrapper = document.createElement("div");
            wrapper.className = "notification-dropdown";
            rawBellLink.parentNode.insertBefore(wrapper, rawBellLink);
            wrapper.appendChild(rawBellLink);
        }

        rawBellLink.classList.add("notification-trigger");
        rawBellLink.setAttribute("aria-expanded", "false");
        rawBellLink.setAttribute("aria-haspopup", "true");

        notificationUnreadDot = rawBellLink.querySelector(".notification-unread-dot");
        if (!notificationUnreadDot) {
            notificationUnreadDot = document.createElement("span");
            notificationUnreadDot.className = "notification-unread-dot";
            notificationUnreadDot.hidden = true;
            rawBellLink.appendChild(notificationUnreadDot);
        }

        notificationPopup = headerActions.querySelector("#notificationPopup");
        if (!notificationPopup) {
            notificationPopup = document.createElement("div");
            notificationPopup.id = "notificationPopup";
            notificationPopup.className = "notification-popup";
            notificationPopup.setAttribute("role", "dialog");
            notificationPopup.setAttribute("aria-label", "Notifications");
            notificationPopup.hidden = true;
            notificationPopup.innerHTML = `
                <div class="notification-popup-header">
                    <span class="notification-popup-title">Notification</span>
                </div>
                <div class="notification-list" id="notificationList"></div>
            `;
            headerActions.appendChild(notificationPopup);
        }

        notificationListEl = notificationPopup.querySelector("#notificationList");

        rawBellLink.addEventListener("click", async (event) => {
            event.preventDefault();
            event.stopPropagation();

            if (loggedInMenu) loggedInMenu.hidden = true;
            if (guestMenu) guestMenu.hidden = true;
            if (profileBtn) profileBtn.setAttribute("aria-expanded", "false");

            isNotificationOpen = !isNotificationOpen;
            notificationPopup.hidden = !isNotificationOpen;
            rawBellLink.setAttribute("aria-expanded", String(isNotificationOpen));

            if (isNotificationOpen) {
                await loadNotifications();
                await markAllNotificationsRead();
            }
        });

        loadNotifications();
        setInterval(loadNotifications, 30000);
        window.addEventListener("focus", loadNotifications);
    }

    setupNotificationUI();
    window.refreshNotifications = loadNotifications;

    // Auto-refresh notifications after mutating /api/events requests (join, leave, cancel, create)
    if (typeof window.fetch === "function" && !window.__tumeetNotifFetchWrapped) {
        const originalFetch = window.fetch.bind(window);
        window.__tumeetNotifFetchWrapped = true;
        window.fetch = async function (input, init) {
            const response = await originalFetch(input, init);
            try {
                const url = typeof input === "string" ? input : (input && input.url) || "";
                const method = String((init && init.method) || "GET").toUpperCase();
                if (url.includes("/api/events") && method !== "GET" && response.ok) {
                    setTimeout(() => {
                        if (typeof window.refreshNotifications === "function") {
                            window.refreshNotifications();
                        }
                    }, 150);
                }
            } catch (_) {}
            return response;
        };
    }

    // สถานะ Login สำหรับทดสอบ
    const isLoggedIn =  true;

    // ตรวจสอบ Element
    if (!profileBtn || !loggedInMenu || !guestMenu) {
        console.error("ไม่พบ Profile Dropdown Element");
        return;
    }

    // แสดงเมนูตามสถานะ Login
    function updateMenu() {
        loggedInMenu.hidden = true;
        guestMenu.hidden = true;
    }

    updateMenu();

    // กดปุ่ม Profile
    profileBtn.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();

        const currentMenu = isLoggedIn
            ? loggedInMenu
            : guestMenu;

        const isAlreadyOpen = !currentMenu.hidden;

        loggedInMenu.hidden = true;
        guestMenu.hidden = true;
        if (notificationPopup) {
            notificationPopup.hidden = true;
            isNotificationOpen = false;
            if (rawBellLink) rawBellLink.setAttribute("aria-expanded", "false");
        }

        currentMenu.hidden = isAlreadyOpen;

        profileBtn.setAttribute(
            "aria-expanded",
            String(!isAlreadyOpen)
        );
    });

    // คลิกพื้นที่อื่นเพื่อปิด
    document.addEventListener("click", (event) => {
        if (!event.target.closest(".profile-dropdown")) {
            loggedInMenu.hidden = true;
            guestMenu.hidden = true;

            profileBtn.setAttribute("aria-expanded", "false");
        }
        if (notificationPopup && !event.target.closest(".notification-dropdown") && !event.target.closest("#notificationPopup")) {
            notificationPopup.hidden = true;
            isNotificationOpen = false;
            if (rawBellLink) rawBellLink.setAttribute("aria-expanded", "false");
        }
    });

    // Logout
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            if (confirm("ต้องการออกจากระบบหรือไม่?")) {
                try {
                    localStorage.removeItem("loginData");
                    localStorage.removeItem("studentId");
                    localStorage.setItem("tumeet_logged_out", "true");
                    sessionStorage.removeItem("loginData");
                    sessionStorage.removeItem("studentId");
                } catch (_) {}
                window.location.replace("./Login.html");
            }
        });
    }

});
