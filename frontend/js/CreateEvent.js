// จัดการการส่งฟอร์มและเชื่อมต่อ API
const eventForm = document.querySelector('.event-form');

eventForm.addEventListener('submit', async function(event) {
    // 1. ป้องกันไม่ให้หน้าเว็บรีเฟรช
    event.preventDefault();

    // 2. ดึงค่าจากฟอร์ม
    const eventType = document.getElementById('eventType').value;
    const eventName = document.getElementById('eventName').value.trim();
    const eventSize = document.getElementById('eventSize').value;
    const eventDate = document.getElementById('eventDate').value;
    const eventTime = document.getElementById('eventTime').value;

    const submitBtn = document.querySelector('.btn-create');
    // สมมติว่าใน HTML หน้า Create Event มี <div id="errorBox" class="hidden"></div> สำหรับแสดงข้อความเตือน
    const errorBox = document.getElementById('errorBox'); 

    // 3. เตรียมสถานะระหว่างรอข้อมูลจาก Backend
    if (errorBox) errorBox.classList.add('hidden');
    submitBtn.disabled = true;
    submitBtn.textContent = 'CREATING...';

    // 4. ดึง Token ยืนยันตัวตน (ที่บันทึกไว้หลังจาก Login สำเร็จ)
    const token = localStorage.getItem('token');

    try {
        // 5. ส่งข้อมูลไปยัง Backend
        const response = await fetch('http://localhost:8080/api/login', { 
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : '' // แนบ Token ไปยืนยันตัวตน
            },
            body: JSON.stringify({
                eventType: eventType,
                eventName: eventName,
                eventSize: parseInt(eventSize, 10),
                eventDate: eventDate,
                eventTime: eventTime
            })
        });

        const data = await response.json();

        // 6. ตรวจสอบสถานะการตอบกลับ
        if (!response.ok) {
            throw new Error(data.message || 'ไม่สามารถสร้างกิจกรรมได้ กรุณาลองใหม่อีกครั้ง');
        }

        // กรณีสำเร็จ
        alert('สร้างกิจกรรมสำเร็จ!');
        eventForm.reset(); // ล้างข้อมูลในฟอร์ม
        // window.location.href = '/dashboard.html'; // พากลับหน้า Dashboard หรือตามที่ตกลงกันไว้

    } catch (error) {
        // 7. แสดงข้อความแจ้งเตือนเมื่อเกิดข้อผิดพลาด
        if (errorBox) {
            errorBox.textContent = error.message || "Failed to create event, please try again.";
            errorBox.classList.remove('hidden');
        } else {
            alert(error.message || "Failed to create event, please try again.");
        }
    } finally {
        // 8. คืนค่าปุ่มให้ใช้งานได้ปกติ
        submitBtn.disabled = false;
        submitBtn.textContent = 'CREATE';
    }
});

// ดึง element ของช่องกรอกข้อมูล เพื่อตั้งค่า Custom Validity (เหมือนโค้ดของเพื่อน)
const eventTypeInput = document.getElementById('eventType');
const eventNameInput = document.getElementById('eventName');
const eventSizeInput = document.getElementById('eventSize');
const eventDateInput = document.getElementById('eventDate');
const eventTimeInput = document.getElementById('eventTime');

// เปลี่ยนข้อความเตือนเมื่อผู้ใช้ไม่ได้กรอกข้อมูล
eventTypeInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณาเลือกหมวดหมู่กิจกรรม');
};
eventTypeInput.onchange = function(event) {
    event.target.setCustomValidity('');
};

eventNameInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณากรอกชื่อกิจกรรม');
};
eventNameInput.oninput = function(event) {
    event.target.setCustomValidity('');
};

eventSizeInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณาระบุจำนวนคน');
};
eventSizeInput.oninput = function(event) {
    event.target.setCustomValidity('');
};

eventDateInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณาเลือกวันที่');
};
eventDateInput.oninput = function(event) {
    event.target.setCustomValidity('');
};

eventTimeInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณาเลือกเวลา');
};
eventTimeInput.oninput = function(event) {
    event.target.setCustomValidity('');
};