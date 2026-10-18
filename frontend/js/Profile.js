document.addEventListener("DOMContentLoaded", () => {
  const API_BASE_URL = "http://localhost:8080/api";

  // Elements
  const usernameDisplayBox = document.getElementById("usernameDisplayBox");
  const usernameDisplay = document.getElementById("usernameDisplay");
  const usernameEditWrapper = document.getElementById("usernameEditWrapper");
  const usernameInput = document.getElementById("usernameInput");
  const saveUsernameBtn = document.getElementById("saveUsernameBtn");
  
  const studentIdDisplay = document.getElementById("studentIdDisplay");
  const emailDisplay = document.getElementById("emailDisplay");
  const avatarInput = document.getElementById("avatarInput");
  const avatarImg = document.getElementById("avatarImg");

  // 1. โหลดข้อมูลโปรไฟล์จาก LocalStorage (ที่เซฟไว้ตอน Login จาก API)
  function loadUserProfile() {
    try {
      const storedUserData = JSON.parse(localStorage.getItem("loginData"));

      if (storedUserData) {
        if (storedUserData.studentId) {
          studentIdDisplay.textContent = storedUserData.studentId;
        }

        // ดึงอีเมลเต็มจาก API Login มาใส่ได้เลย
        if (storedUserData.email) {
          emailDisplay.textContent = storedUserData.email;
        }

        if (storedUserData.username) {
          usernameDisplay.textContent = storedUserData.username;
          usernameInput.value = storedUserData.username;
        }

        if (storedUserData.avatarUrl) {
          avatarImg.src = storedUserData.avatarUrl;
        }
      }
    } catch (error) {
      console.error("เกิดข้อผิดพลาดในการโหลดข้อมูลโปรไฟล์:", error);
    }
  }

  // 2. คลิกกล่อง Username เพื่อแก้ไขชื่อ
  if (usernameDisplayBox) {
    usernameDisplayBox.addEventListener("click", () => {
      usernameInput.value = usernameDisplay.textContent.trim();
      usernameDisplayBox.style.display = "none";
      usernameEditWrapper.style.display = "flex";
      usernameInput.focus();
    });
  }

  // 3. บันทึก Username และอัปเดตกลับไปยัง API
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
        headers: {
          "Content-Type": "application/json"
        },
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

  if (saveUsernameBtn) {
    saveUsernameBtn.addEventListener("click", updateUsername);
  }

  if (usernameInput) {
    usernameInput.addEventListener("keyup", (event) => {
      if (event.key === "Enter") {
        updateUsername();
      }
    });
  }

  // 4. เปลี่ยนรูปโปรไฟล์
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

  loadUserProfile();
});