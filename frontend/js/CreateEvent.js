// จัดการการส่งฟอร์มและเชื่อมต่อ API
const eventForm = document.querySelector('.event-form');

eventForm.addEventListener('submit', async function(event) {
    // 1. ป้องกันไม่ให้หน้าเว็บรีเฟรช
    event.preventDefault();

    // 2. ดึงค่าจากฟอร์ม
    const eventType = document.getElementById('eventType').value;
    const eventName = document.getElementById('eventName').value.trim();
    const eventLocationElem = document.getElementById('eventLocation');
    const eventLocation = eventLocationElem ? eventLocationElem.value.trim() : null;
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

    // 4. ดึงข้อมูลผู้ใช้ (ที่บันทึกไว้หลังจาก Login หรือใช้ Mock Student ID: 6800000001)
    let creatorStudentId = '6800000001';
    try {
        const storedLogin = JSON.parse(localStorage.getItem('loginData') || '{}');
        if (storedLogin.studentId) {
            creatorStudentId = storedLogin.studentId;
        } else if (localStorage.getItem('studentId')) {
            creatorStudentId = localStorage.getItem('studentId');
        }
    } catch (e) {
        console.warn('Cannot read login data, using default creator:', e);
    }

    try {
        // 5. ส่งข้อมูลไปยัง Backend
        const response = await fetch('http://localhost:8080/api/events', { 
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                eventType: eventType,
                eventName: eventName,
                location: eventLocation,
                eventLocation: eventLocation,
                eventSize: parseInt(eventSize, 10),
                eventDate: eventDate,
                eventTime: eventTime,
                creatorStudentId: creatorStudentId
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

const eventLocationInput = document.getElementById('eventLocation');
if (eventLocationInput) {
    eventLocationInput.oninvalid = function(event) {
        event.target.setCustomValidity('กรุณากรอกสถานที่จัดกิจกรรม');
    };
    eventLocationInput.oninput = function(event) {
        event.target.setCustomValidity('');
    };
}

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