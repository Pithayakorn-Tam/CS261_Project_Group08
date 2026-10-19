document.addEventListener("DOMContentLoaded", () => {
  const API_BASE_URL = "http://localhost:8080/api";

  // Elements ผู้ใช้
  const usernameDisplayBox = document.getElementById("usernameDisplayBox");
  const usernameDisplay = document.getElementById("usernameDisplay");
  const usernameEditWrapper = document.getElementById("usernameEditWrapper");
  const usernameInput = document.getElementById("usernameInput");
  const saveUsernameBtn = document.getElementById("saveUsernameBtn");

  const studentIdDisplay = document.getElementById("studentIdDisplay");
  const emailDisplay = document.getElementById("emailDisplay");
  const avatarInput = document.getElementById("avatarInput");
  const avatarImg = document.getElementById("avatarImg");

  // Elements Modal
  const cancelModal = document.getElementById("cancelModal");
  const confirmCancelBtn = document.getElementById("confirmCancelBtn");
  const dismissCancelBtn = document.getElementById("dismissCancelBtn");
  let selectedEventIdToCancel = null;

  // Mock Data กิจกรรม
  
  // 1. กิจกรรมปัจจุบัน (สร้างเอง / เข้าร่วม)
  const activeEventsMock = [
    {
      id: 1,
      title: "บอร์ดเกม",
      logo: "../Picture/EntertainmentAcivityLogo.svg",
      time: "12.00",
      date: "10 OCT 2026",
      location: "Tsuki cafe, เชียงราก 2",
      isOwner: true, // สร้างเอง -> มีปุ่ม "รายละเอียด"
      detailUrl: "./EventDetail.html?id=1"
    },
    {
      id: 2,
      title: "ตีแบตมินตัน",
      logo: "../Picture/HealthActivityLogo.svg",
      time: "19.00",
      date: "Sat, 10 Oct",
      location: "ยิม มธ.",
      isOwner: false, // เข้าร่วม -> ขึ้น "เข้าร่วมแล้ว" + ปุ่มกากบาทแดง
      isJoined: true
    }
  ];

  // 2. กิจกรรมในอดีต (เคยสร้าง / เคยเข้าร่วม -> ไม่ต้องมีปุ่มใดๆ)
  const pastEventsMock = [
    {
      id: 3,
      title: "อ่านหนังสือ",
      logo: "../Picture/AcademicActivityLogo.svg",
      time: "19.00",
      date: "7 OCT 2026",
      location: "ห้องสมุดป๋วย"
    },
    {
      id: 4,
      title: "ตี๋น้อย",
      logo: "../Picture/LifestyleActivityLogo.svg",
      time: "19.00",
      date: "8 OCT 2026",
      location: "Mingle, ม.กรุงเทพ"
    },
    {
      id: 5,
      title: "ตี๋น้อย",
      logo: "../Picture/LifestyleActivityLogo.svg",
      time: "19.00",
      date: "9 OCT 2026",
      location: "Mingle, ม.กรุงเทพ"
    }
  ];

  // ฟังก์ชันการจัดการข้อมูลผู้ใช้

  function loadUserProfile() {
    try {
      const storedUserData = JSON.parse(localStorage.getItem("loginData"));

      if (storedUserData) {
        if (storedUserData.studentId) studentIdDisplay.textContent = storedUserData.studentId;
        if (storedUserData.email) emailDisplay.textContent = storedUserData.email;
        if (storedUserData.username) {
          usernameDisplay.textContent = storedUserData.username;
          usernameInput.value = storedUserData.username;
        }
        if (storedUserData.avatarUrl) avatarImg.src = storedUserData.avatarUrl;
      }
    } catch (error) {
      console.error("เกิดข้อผิดพลาดในการโหลดข้อมูลโปรไฟล์:", error);
    }
  }

  if (usernameDisplayBox) {
    usernameDisplayBox.addEventListener("click", () => {
      usernameInput.value = usernameDisplay.textContent.trim();
      usernameDisplayBox.style.display = "none";
      usernameEditWrapper.style.display = "flex";
      usernameInput.focus();
    });
  }

  async function updateUsername() {
    const newUsername = usernameInput.value.trim();

    if (!newUsername) {
      alert("กรุณากรอกชื่อผู้ใช้");
      return;
    }

    usernameDisplay.textContent = newUsername;
    usernameEditWrapper.style.display = "none";
    usernameDisplayBox.style.display = "flex";

    try {
      const storedUserData = JSON.parse(localStorage.getItem("loginData")) || {};

      await fetch(`${API_BASE_URL}/user/update-username`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          studentId: storedUserData.studentId,
          username: newUsername
        })
      });

      storedUserData.username = newUsername;
      localStorage.setItem("loginData", JSON.stringify(storedUserData));
    } catch (error) {
      console.error("เกิดข้อผิดพลาดในการเชื่อมต่อ API:", error);
      const storedUserData = JSON.parse(localStorage.getItem("loginData")) || {};
      storedUserData.username = newUsername;
      localStorage.setItem("loginData", JSON.stringify(storedUserData));
    }
  }

  if (saveUsernameBtn) saveUsernameBtn.addEventListener("click", updateUsername);

  if (usernameInput) {
    usernameInput.addEventListener("keyup", (event) => {
      if (event.key === "Enter") updateUsername();
    });
  }

  if (avatarInput) {
    avatarInput.addEventListener("change", function(event) {
      const file = event.target.files[0];
      if (file) {
        if (!file.type.startsWith("image/")) {
          alert("กรุณาเลือกไฟล์รูปภาพเท่านั้น");
          return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
          const base64Image = e.target.result;
          avatarImg.src = base64Image;

          const storedUserData = JSON.parse(localStorage.getItem("loginData")) || {};
          storedUserData.avatarUrl = base64Image;
          localStorage.setItem("loginData", JSON.stringify(storedUserData));
        };

        reader.readAsDataURL(file);
      }
    });
  }

  // ฟังก์ชันแสดงผลและจัดการการ์ดกิจกรรม

  function createEventCardHTML(event, isPast = false) {
    let actionHTML = "";

    if (!isPast) {
      if (event.isOwner) {
        actionHTML = `
          <div class="event-action-right">
            <a href="${event.detailUrl || '#'}" class="detail-link-btn">รายละเอียด</a>
          </div>
        `;
      } else if (event.isJoined) {
        actionHTML = `
          <div class="event-action-right joined-action">
            <span class="joined-status-text">เข้าร่วมแล้ว</span>
            <button type="button" class="cancel-event-btn" data-id="${event.id}" aria-label="ยกเลิกการเข้าร่วม">✕</button>
          </div>
        `;
      }
    }

    return `
      <div class="event-card" data-event-id="${event.id}">
        <div class="event-logo">
          <img src="${event.logo}" alt="${event.title}">
        </div>
        <div class="event-info">
          <div class="event-badge">${event.title}</div>
          <div class="event-time-date">
            <span><img src="../Picture/TimeIcon.svg" class="detail-icon" alt="Time"> ${event.time}</span>
            <span><img src="../Picture/CalendarIcon.svg" class="detail-icon" alt="Calendar"> ${event.date}</span>
          </div>
          <div class="event-location">
            <img src="../Picture/LocationIcon.svg" class="detail-icon" alt="Location"> ${event.location}
          </div>
        </div>
        ${actionHTML}
      </div>
    `;
  }

  function renderEvents() {
    const activeContainer = document.getElementById("activeEventsList");
    const pastContainer = document.getElementById("pastEventsList");

    if (activeContainer) {
      activeContainer.innerHTML = activeEventsMock
        .map(event => createEventCardHTML(event, false))
        .join("");
    }

    if (pastContainer) {
      pastContainer.innerHTML = pastEventsMock
        .map(event => createEventCardHTML(event, true))
        .join("");
    }

    bindCancelButtons();
  }

  function bindCancelButtons() {
    const cancelBtns = document.querySelectorAll(".cancel-event-btn");
    cancelBtns.forEach(btn => {
      btn.addEventListener("click", (e) => {
        selectedEventIdToCancel = e.target.getAttribute("data-id");
        if (cancelModal) cancelModal.style.display = "flex";
      });
    });
  }

  if (dismissCancelBtn) {
    dismissCancelBtn.addEventListener("click", () => {
      if (cancelModal) cancelModal.style.display = "none";
      selectedEventIdToCancel = null;
    });
  }

  if (confirmCancelBtn) {
    confirmCancelBtn.addEventListener("click", () => {
      if (selectedEventIdToCancel) {
        const index = activeEventsMock.findIndex(item => item.id == selectedEventIdToCancel);
        if (index !== -1) {
          activeEventsMock.splice(index, 1);
        }
        renderEvents();
      }

      if (cancelModal) cancelModal.style.display = "none";
      selectedEventIdToCancel = null;
    });
  }

  // เรียกใช้อิมพีเมนต์เมื่อโหลดหน้าเว็บ
  loadUserProfile();
  renderEvents();
});