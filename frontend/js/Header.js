
document.addEventListener("DOMContentLoaded", () => {

    const profileBtn = document.getElementById("profileBtn");
    const loggedInMenu = document.getElementById("loggedInMenu");
    const guestMenu = document.getElementById("guestMenu");
    const logoutBtn = document.getElementById("logoutBtn");

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
