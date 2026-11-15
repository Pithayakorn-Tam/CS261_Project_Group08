
document.addEventListener("DOMContentLoaded", () => {

    const API_BASE_URL = (window.location.protocol === "file:" || window.location.port === "5500" || window.location.port === "5501")
        ? "http://localhost:8080/api"
        : "/api";

    const profileBtn = document.getElementById("profileBtn");
    const loggedInMenu = document.getElementById("loggedInMenu");
    const guestMenu = document.getElementById("guestMenu");
    const logoutBtn = document.getElementById("logoutBtn");

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
            const studentId = stored.studentId || localStorage.getItem("studentId");
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
    });

    // Logout
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            if (confirm("ต้องการออกจากระบบหรือไม่?")) {
                window.location.href = "./Login.html";
            }
        });
    }

});
