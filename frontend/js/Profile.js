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

  // Elements Modal ยกเลิกการเข้าร่วมกิจกรรม
  const cancelModal = document.getElementById("cancelModal");
  const confirmCancelBtn = document.getElementById("confirmCancelBtn");
  const dismissCancelBtn = document.getElementById("dismissCancelBtn");
  let selectedEventIdToCancel = null;

  // ตารางจับคู่หมวดหมู่กับโลโก้
  const categoryLogos = {
    study: "../Picture/AcademicActivityLogo.svg",
    วิชาการ: "../Picture/AcademicActivityLogo.svg",
    sport: "../Picture/HealthActivityLogo.svg",
    กีฬา: "../Picture/HealthActivityLogo.svg",
    bordgame: "../Picture/EntertainmentAcivityLogo.svg",
    "E-sport": "../Picture/EntertainmentAcivityLogo.svg",
    consert: "../Picture/EntertainmentAcivityLogo.svg",
    บันเทิง: "../Picture/EntertainmentAcivityLogo.svg",
    food: "../Picture/LifestyleActivityLogo.svg",
    other: "../Picture/LifestyleActivityLogo.svg",
    ไลฟ์สไตล์: "../Picture/LifestyleActivityLogo.svg",
    default: "../Picture/LogoTUmeet.svg"
  };

  // รายการกิจกรรมดึงจากฐานข้อมูล 100% (ไม่มี Mock ในฝั่ง Frontend)
  let activeEvents = [];
  let pastEvents = [];

  function getCurrentStudentId() {
    try {
      const storedUserData = JSON.parse(localStorage.getItem("loginData") || "{}");
      if (storedUserData.studentId) return storedUserData.studentId;
      if (localStorage.getItem("studentId")) return localStorage.getItem("studentId");
    } catch (e) {
      console.warn("Cannot parse loginData:", e);
    }
    return "6800000000";
  }

  function formatEventDate(dateStr) {
    if (!dateStr) return "-";
    if (/[a-zA-Z]/.test(dateStr)) return dateStr;

    const parts = String(dateStr).split("-");
    if (parts.length !== 3) return dateStr;

    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1;
    const day = parseInt(parts[2], 10);

    const dateObj = new Date(year, month, day);
    if (isNaN(dateObj.getTime())) return dateStr;

    const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

    return `${days[dateObj.getDay()]}, ${day} ${months[dateObj.getMonth()]}`;
  }

  function getEventTimestamp(dto) {
    if (!dto) return 0;
    const dateStr = dto.eventDate || dto.date;
    if (!dateStr) return 0;

    const parts = String(dateStr).split("-");
    if (parts.length !== 3) return 0;

    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1;
    const day = parseInt(parts[2], 10);

    let hours = 23;
    let minutes = 59;
    const timeStr = String(dto.eventTime || dto.time || "").trim();
    if (timeStr) {
      const timeParts = timeStr.replace(".", ":").split(":");
      if (timeParts.length >= 2) {
        const h = parseInt(timeParts[0], 10);
        const m = parseInt(timeParts[1], 10);
        if (!isNaN(h) && !isNaN(m)) {
          hours = h;
          minutes = m;
        }
      }
    }

    const eventDateTime = new Date(year, month, day, hours, minutes, 59);
    return isNaN(eventDateTime.getTime()) ? 0 : eventDateTime.getTime();
  }

  // ตรวจสอบว่ากิจกรรมหมดเวลาแล้วหรือไม่
  function isEventExpired(dto) {
    if (!dto) return false;
    if (dto.status === "CLOSED" || dto.status === "CANCELLED") {
      return true;
    }
    const ts = getEventTimestamp(dto);
    if (ts === 0) return false;
    return ts < Date.now();
  }

  function mapDtoToProfileEvent(dto, isOwner, isJoined) {
    const typeKey = dto.eventType || "default";
    const expired = isEventExpired(dto);
    return {
      id: dto.id,
      title: dto.eventName || "-",
      logo: categoryLogos[typeKey] || categoryLogos.default,
      time: dto.eventTime || "-",
      date: formatEventDate(dto.eventDate),
      rawDate: dto.eventDate,
      timestamp: getEventTimestamp(dto),
      location: dto.location || "-",
      status: dto.status || "OPEN",
      expired: expired,
      isOwner: isOwner,
      isJoined: isJoined
    };
  }

  // ฟังก์ชันโหลดข้อมูลโปรไฟล์ผู้ใช้
  async function loadUserProfile() {
    const studentId = getCurrentStudentId();
    try {
      const storedUserData = JSON.parse(localStorage.getItem("loginData") || "{}");

      if (studentIdDisplay) studentIdDisplay.textContent = storedUserData.studentId || studentId;
      if (emailDisplay && storedUserData.email) emailDisplay.textContent = storedUserData.email;
      if (storedUserData.username) {
        if (usernameDisplay) usernameDisplay.textContent = storedUserData.username;
        if (usernameInput) usernameInput.value = storedUserData.username;
      }
      if (storedUserData.avatarUrl && avatarImg) {
        avatarImg.src = storedUserData.avatarUrl;
      }

      const res = await fetch(`${API_BASE_URL}/user/${encodeURIComponent(studentId)}`);
      if (res.ok) {
        const result = await res.json();
        const user = result.data;
        if (user) {
          const resolvedUsername = user.username || user.displayname_th || storedUserData.username || studentId;
          const resolvedEmail = user.email || storedUserData.email || `${studentId}@dome.tu.ac.th`;
          if (studentIdDisplay) studentIdDisplay.textContent = user.studentId || studentId;
          if (emailDisplay) emailDisplay.textContent = resolvedEmail;
          if (usernameDisplay) usernameDisplay.textContent = resolvedUsername;
          if (usernameInput) usernameInput.value = resolvedUsername;

          localStorage.setItem(
            "loginData",
            JSON.stringify({
              ...storedUserData,
              studentId: user.studentId || studentId,
              email: resolvedEmail,
              username: resolvedUsername
            })
          );
        }
      }
    } catch (error) {
      console.warn("เกิดข้อผิดพลาดในการซิงค์ข้อมูลโปรไฟล์:", error);
    }
  }

  // ฟังก์ชันดึงกิจกรรมที่ผู้ใช้สร้างและเข้าร่วมจริงจากฐานข้อมูล 100%
  async function loadUserEvents() {
    const studentId = getCurrentStudentId();

    try {
      const [joinedRes, createdRes] = await Promise.all([
        fetch(`${API_BASE_URL}/events?participantStudentId=${encodeURIComponent(studentId)}`),
        fetch(`${API_BASE_URL}/events?creatorStudentId=${encodeURIComponent(studentId)}`)
      ]);

      const joinedData = joinedRes.ok ? await joinedRes.json() : { data: [] };
      const createdData = createdRes.ok ? await createdRes.json() : { data: [] };

      const joinedDtos = Array.isArray(joinedData.data) ? joinedData.data : [];
      const createdDtos = Array.isArray(createdData.data) ? createdData.data : [];

      const eventMap = new Map();

      // 1. กิจกรรมที่ผู้ใช้กดเข้าร่วม
      joinedDtos.forEach(dto => {
        if (dto && dto.id != null) {
          eventMap.set(dto.id, mapDtoToProfileEvent(dto, false, true));
        }
      });

      // 2. กิจกรรมที่ผู้ใช้สร้าง (ยกเว้น 4 กิจกรรมตั้งต้น id 1-4 หากไม่ได้กดเข้าร่วมเอง)
      createdDtos.forEach(dto => {
        if (dto && dto.id != null && !eventMap.has(dto.id)) {
          const isSystemDefaultSeeded = dto.id <= 4 && (studentId === "6800000000" || studentId === "6800000001");
          if (!isSystemDefaultSeeded) {
            eventMap.set(dto.id, mapDtoToProfileEvent(dto, true, false));
          }
        }
      });

      const allUserEvents = Array.from(eventMap.values());

      // แยกกิจกรรมที่ยังไม่หมดเวลา (Active) และที่หมดเวลาแล้ว (Past) พร้อมเรียงลำดับ
      activeEvents = allUserEvents
        .filter(ev => !ev.expired)
        .sort((a, b) => {
          const isUserCreatedA = (Number(a.id) || 0) > 4;
          const isUserCreatedB = (Number(b.id) || 0) > 4;
          if (isUserCreatedA !== isUserCreatedB) {
            return isUserCreatedA ? -1 : 1;
          }
          if (isUserCreatedA && isUserCreatedB) {
            const idDiff = (Number(b.id) || 0) - (Number(a.id) || 0);
            if (idDiff !== 0) return idDiff;
          }
          return a.timestamp - b.timestamp;
        });

      pastEvents = allUserEvents
        .filter(ev => ev.expired)
        .sort((a, b) => b.timestamp - a.timestamp);

      renderEvents();
    } catch (error) {
      console.error("ไม่สามารถดึงข้อมูลกิจกรรมจากฐานข้อมูลได้:", error);
      activeEvents = [];
      pastEvents = [];
      renderEvents();
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

    const studentId = getCurrentStudentId();
    try {
      const storedUserData = JSON.parse(localStorage.getItem("loginData") || "{}");

      await fetch(`${API_BASE_URL}/user/update-username`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          studentId: studentId,
          username: newUsername
        })
      });

      storedUserData.studentId = studentId;
      storedUserData.username = newUsername;
      localStorage.setItem("loginData", JSON.stringify(storedUserData));
    } catch (error) {
      console.error("เกิดข้อผิดพลาดในการเชื่อมต่อ API:", error);
      const storedUserData = JSON.parse(localStorage.getItem("loginData") || "{}");
      storedUserData.studentId = studentId;
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

          const storedUserData = JSON.parse(localStorage.getItem("loginData") || "{}");
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

    // กิจกรรมที่ยังไม่หมดเวลา: แสดงสถานะและปุ่มกากบาทสีแดง ✕ เพื่อให้กดยกเลิกได้
    if (!isPast && !event.expired) {
      const statusLabel = event.isJoined ? "เข้าร่วมแล้ว" : "สร้างโดยคุณ";
      actionHTML = `
        <div class="event-action-right joined-action">
          <span class="joined-status-text">${statusLabel}</span>
          <button type="button" class="cancel-event-btn" data-id="${event.id}" aria-label="ยกเลิกการเข้าร่วม">✕</button>
        </div>
      `;
    }

    const cardClass = (isPast || event.expired) ? "event-card expired" : "event-card";

    return `
      <div class="${cardClass}" data-event-id="${event.id}">
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
      if (activeEvents.length === 0) {
        activeContainer.innerHTML = `<div style="text-align:center; color:#888; padding: 16px 0; font-size: 14px;">ยังไม่มีกิจกรรมที่คุณสร้างหรือเข้าร่วม</div>`;
      } else {
        activeContainer.innerHTML = activeEvents
          .map(event => createEventCardHTML(event, false))
          .join("");
      }
    }

    if (pastContainer) {
      if (pastEvents.length === 0) {
        pastContainer.innerHTML = `<div style="text-align:center; color:#888; padding: 16px 0; font-size: 14px;">ยังไม่มีประวัติกิจกรรมในอดีต</div>`;
      } else {
        pastContainer.innerHTML = pastEvents
          .map(event => createEventCardHTML(event, true))
          .join("");
      }
    }

    bindCancelButtons();
  }

  function bindCancelButtons() {
    const cancelBtns = document.querySelectorAll(".cancel-event-btn");
    cancelBtns.forEach(btn => {
      btn.addEventListener("click", (e) => {
        selectedEventIdToCancel = e.currentTarget.getAttribute("data-id");
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
    confirmCancelBtn.addEventListener("click", async () => {
      if (!selectedEventIdToCancel) return;

      const eventId = Number(selectedEventIdToCancel);
      const studentId = getCurrentStudentId();
      confirmCancelBtn.disabled = true;

      try {
        const response = await fetch(`${API_BASE_URL}/events/${eventId}/leave`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ studentId: studentId })
        });

        const data = await response.json();

        if (!response.ok || data.status === false) {
          throw new Error(data.message || "ไม่สามารถยกเลิกการเข้าร่วมกิจกรรมได้");
        }

        if (cancelModal) cancelModal.style.display = "none";
        selectedEventIdToCancel = null;

        // ดึงข้อมูลล่าสุดจากฐานข้อมูลมาแสดงผลใหม่ทันที
        await loadUserEvents();
      } catch (error) {
        console.error("Error cancelling event participation:", error);
        if (cancelModal) cancelModal.style.display = "none";
        selectedEventIdToCancel = null;
        alert(error.message || "เกิดข้อผิดพลาดในการยกเลิกการเข้าร่วมกิจกรรม");
      } finally {
        confirmCancelBtn.disabled = false;
      }
    });
  }

  // โหลดข้อมูลโปรไฟล์และรายการกิจกรรมจากฐานข้อมูลเมื่อเปิดหน้าเว็บ
  loadUserProfile();
  loadUserEvents();
});
