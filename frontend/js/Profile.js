document.addEventListener("DOMContentLoaded", () => {
  const API_BASE_URL = (window.location.protocol === "file:" || window.location.port === "5500" || window.location.port === "5501")
    ? "http://localhost:8080/api"
    : "/api";

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
  const cancelModalTitle = document.getElementById("cancelModalTitle");
  const cancelModalDesc = document.getElementById("cancelModalDesc");
  const confirmCancelBtn = document.getElementById("confirmCancelBtn");
  const dismissCancelBtn = document.getElementById("dismissCancelBtn");
  let selectedEventIdToCancel = null;

// ตารางจับคู่หมวดหมู่กับโลโก้ (ดึงให้เหมือน Homepage)
  const categoryLogos = {
    // วิชาการ
    academic: '../Picture/book-one.svg',
    study: '../Picture/book-one.svg',
    วิชาการ: '../Picture/book-one.svg',

    // กีฬา
    sport: '../Picture/activity.svg',
    กีฬา: '../Picture/activity.svg',

    // บันเทิง
    entertainment: '../Picture/gamecontroller.svg',
    'E-sport': '../Picture/gamecontroller.svg',
    concert: '../Picture/gamecontroller.svg',
    bordgame: '../Picture/gamecontroller.svg',
    บันเทิง: '../Picture/gamecontroller.svg',

    // ไลฟ์สไตล์
    'life-style': '../Picture/bowl.svg',
    lifestyle: '../Picture/bowl.svg',
    food: '../Picture/bowl.svg',
    ไลฟ์สไตล์: '../Picture/bowl.svg',

    // อื่นๆ
    other: '../Picture/bowl.svg',

    // รูปสำรอง (Default)
    default: '../Picture/bowl.svg'
  };

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

  function isEventCancelled(dto) {
    if (!dto) return false;
    return dto.status === "CANCELLED" || dto.lifecycleStatus === "CANCELLED";
  }

  function isEventExpired(dto) {
    if (!dto) return false;
    if (dto.status === "CLOSED" || dto.lifecycleStatus === "COMPLETED") {
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

          if (typeof window.renderUserBadgeFromData === "function") {
            window.renderUserBadgeFromData(user);
          }

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

  // ====================================================
  // สวิตช์ปิด-เปิดข้อมูลจำลอง (Toggle Mock Data)
  // ====================================================
  // - ใส่ true เพื่อดู Mock Data 10 อัน
  // - ใส่ false เพื่อดึงข้อมูลจริงจาก Backend API
  const USE_MOCK_DATA = false;

  async function loadUserEvents() {
    const studentId = getCurrentStudentId();

    if (USE_MOCK_DATA) {
      const mockJoinedDtos = [
        { id: 101, eventName: 'เตะบอลหญ้าเทียม', eventType: 'sport', eventTime: '17:00', eventDate: '2026-11-20', location: 'สนามยิม 7', status: 'OPEN' },
        { id: 103, eventName: 'ดูหนังรอบดึก', eventType: 'entertainment', eventTime: '20:00', eventDate: '2026-11-18', location: 'Zpell', status: 'OPEN' },
        { id: 201, eventName: 'แข่ง Valorant', eventType: 'E-sport', eventTime: '10:00', eventDate: '2026-10-15', location: 'Mingle', status: 'CLOSED' },
        { id: 203, eventName: 'ตีแบดมินตัน', eventType: 'กีฬา', eventTime: '18:00', eventDate: '2026-11-01', location: 'ยิมเนเซียม 4', status: 'CLOSED' },
        { id: 205, eventName: 'คอนเสิร์ตลานจอดรถ', eventType: 'concert', eventTime: '19:00', eventDate: '2026-09-05', location: 'ลานจอดรถ', status: 'CLOSED' }
      ];

      const mockCreatedDtos = [
        { id: 102, eventName: 'ติวแคลคูลัส', eventType: 'study', eventTime: '13:00', eventDate: '2026-11-16', location: 'หอสมุดป๋วยฯ', status: 'OPEN' },
        { id: 104, eventName: 'หาเพื่อนกินหมูกระทะ', eventType: 'food', eventTime: '18:30', eventDate: '2026-11-15', location: 'อู๊ดอู๊ด หมูกระทะ', status: 'OPEN' },
        { id: 105, eventName: 'เล่นบอร์ดเกม Avalon', eventType: 'bordgame', eventTime: '19:00', eventDate: '2026-11-14', location: 'ร้าน Dice Cup', status: 'OPEN' },
        { id: 202, eventName: 'เดินตลาดนัดอินเตอร์โซน', eventType: 'ไลฟ์สไตล์', eventTime: '17:00', eventDate: '2026-11-05', location: 'อินเตอร์โซน', status: 'CLOSED' },
        { id: 204, eventName: 'ถ่ายรูปรับปริญญา', eventType: 'other', eventTime: '09:00', eventDate: '2026-10-20', location: 'คณะวิทย์ฯ', status: 'CLOSED' }
      ];

      try {
        await new Promise(resolve => setTimeout(resolve, 300));
        const eventMap = new Map();

        mockJoinedDtos.forEach(dto => {
          eventMap.set(dto.id, mapDtoToProfileEvent(dto, false, true));
        });

        mockCreatedDtos.forEach(dto => {
          if (!eventMap.has(dto.id)) {
            eventMap.set(dto.id, mapDtoToProfileEvent(dto, true, false));
          }
        });

        const allUserEvents = Array.from(eventMap.values());

        activeEvents = allUserEvents
          .filter(ev => !ev.expired)
          .sort((a, b) => a.timestamp - b.timestamp);

        pastEvents = allUserEvents
          .filter(ev => ev.expired)
          .sort((a, b) => b.timestamp - a.timestamp);

        renderEvents();
      } catch (error) {
        console.error("Error loading mock events:", error);
      }
      return; 
    }

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

      joinedDtos.forEach(dto => {
        if (dto && dto.id != null && !isEventCancelled(dto)) {
          eventMap.set(dto.id, mapDtoToProfileEvent(dto, false, true));
        }
      });

      createdDtos.forEach(dto => {
        if (dto && dto.id != null && !isEventCancelled(dto) && !eventMap.has(dto.id)) {
          eventMap.set(dto.id, mapDtoToProfileEvent(dto, true, false));
        }
      });

      const allUserEvents = Array.from(eventMap.values());

      activeEvents = allUserEvents
        .filter(ev => !ev.expired)
        .sort((a, b) => {
          if (a.isOwner !== b.isOwner) {
            return a.isOwner ? -1 : 1;
          }
          return a.timestamp - b.timestamp;
        });

      pastEvents = allUserEvents
        .filter(ev => ev.expired)
        .sort((a, b) => b.timestamp - a.timestamp);

      renderEvents();
      if (typeof window.refreshUserBadge === "function") {
        window.refreshUserBadge();
      }
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
    avatarInput.addEventListener("change", function (event) {
      const file = event.target.files[0];
      if (file) {
        if (!file.type.startsWith("image/")) {
          alert("กรุณาเลือกไฟล์รูปภาพเท่านั้น");
          return;
        }

        const reader = new FileReader();
        reader.onload = function (e) {
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

  // ====================================================
  // ฟังก์ชันสร้างการ์ด HTML แบบเดียวกับหน้า Home 
  // ====================================================
  function createEventCardHTML(event, isPast = false) {
    let actionHTML = "";

    // ถ้ายังไม่หมดเวลา แสดงสถานะ + ปุ่มกากบาท
    if (!isPast && !event.expired) {
      const statusLabel = event.isJoined ? "เข้าร่วมแล้ว" : "สร้างโดยคุณ";
      actionHTML = `
        <div class="event-action-right joined-action">
          <span class="joined-status-text">${statusLabel}</span>
          <button type="button" class="cancel-event-btn" data-id="${event.id}" data-is-owner="${event.isOwner ? "true" : "false"}" aria-label="${event.isOwner ? "ยกเลิกการสร้างกิจกรรม" : "ยกเลิกการเข้าร่วม"}">✕</button>
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
          <!-- Event Name + Info Button -->
          <div class="event-badge">
            <span class="event-title">${event.title}</span>
            <button class="event-info-btn" type="button" data-event-id="${event.id}" aria-label="ดูรายละเอียดกิจกรรม">
              <img src="../Picture/Info.svg" alt="Info">
            </button>
          </div>
          <!-- Time + Date -->
          <div class="event-time-date">
            <span>
              <img src="../Picture/TimeIcon.svg" class="detail-icon" alt="Time">
              ${event.time}
            </span>
            <span>
              <img src="../Picture/CalendarIcon.svg" class="detail-icon" alt="Calendar">
              ${event.date}
            </span>
          </div>
          <!-- Location -->
          <div class="event-location">
            <img src="../Picture/LocationIcon.svg" class="detail-icon" alt="Location">
            ${event.location}
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
        activeContainer.innerHTML = `
    <div class="empty-message">
      ยังไม่มีกิจกรรมที่คุณสร้างหรือเข้าร่วม
    </div>`;
      } else {
        activeContainer.innerHTML = activeEvents
          .map(event => createEventCardHTML(event, false))
          .join("");
      }
    }

    if (pastContainer) {
      if (pastEvents.length === 0) {
        pastContainer.innerHTML = `
    <div class="empty-message">
      ยังไม่มีประวัติกิจกรรมในอดีต
    </div>`;
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
        const isOwner = e.currentTarget.getAttribute("data-is-owner") === "true";
        if (cancelModalTitle) {
          cancelModalTitle.textContent = isOwner ? "ยกเลิกการสร้างกิจกรรม" : "ยกเลิกการเข้าร่วมกิจกรรม";
        }
        if (cancelModalDesc) {
          cancelModalDesc.textContent = isOwner
            ? "ท่านยืนยันที่จะยกเลิกการสร้างกิจกรรมนี้หรือไม่"
            : "ท่านยืนยันที่จะยกเลิกการเข้าร่วมกิจกรรมหรือไม่";
        }
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

  loadUserProfile();
  loadUserEvents();
});