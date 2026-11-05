// จัดการการส่งฟอร์มและเชื่อมต่อ API
const eventForm = document.querySelector('.event-form');
const timeInput = document.getElementById('eventTime');
const dateInput = document.getElementById('eventDate');

function formatDateYYYYMMDD(dateObj) {
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// กำหนดเงื่อนไขวันที่ขั้นต่ำเป็นวันนี้ และสูงสุดไม่เกิน 7 วันล่วงหน้า
if (dateInput) {
    const today = new Date();
    const maxDate = new Date(today);
    maxDate.setDate(today.getDate() + 7);
    dateInput.min = formatDateYYYYMMDD(today);
    dateInput.max = formatDateYYYYMMDD(maxDate);
}

eventForm.addEventListener('submit', async function(event) {
    // 1. ป้องกันไม่ให้หน้าเว็บรีเฟรช
    event.preventDefault();

    // ล้างข้อความแจ้งเตือนเก่าทั้งหมดก่อนตรวจสอบใหม่
    document.querySelectorAll('.error-message').forEach(el => el.textContent = '');
    const errorBox = document.getElementById('errorBox');
    if (errorBox) {
        errorBox.textContent = '';
        errorBox.classList.add('hidden');
    }

    // 2. ดึงค่าจากฟอร์ม
    const eventType = document.getElementById('eventType').value;
    const eventName = document.getElementById('eventName').value.trim();
    const eventLocation = document.getElementById('eventLocation').value.trim();
    const eventDescription = document.getElementById('eventDescription') ? document.getElementById('eventDescription').value.trim() : '';
    const eventSize = document.getElementById('eventSize').value;
    const eventDate = dateInput.value;
    const eventTime = timeInput.value;

    let isValid = true;

    // ตรวจสอบความถูกต้องทีละช่อง (แสดงข้อความสีแดงใต้ช่องนั้นๆ)
    if (!eventType) {
        document.getElementById('typeError').textContent = 'กรุณาเลือกหมวดหมู่กิจกรรม';
        isValid = false;
    }
    if (!eventName) {
        document.getElementById('nameError').textContent = 'กรุณากรอกชื่อกิจกรรม';
        isValid = false;
    }
    if (!eventLocation) {
        document.getElementById('locationError').textContent = 'กรุณากรอกสถานที่จัดกิจกรรม';
        isValid = false;
    }
    if (!eventDescription) {
        document.getElementById('descError').textContent = 'กรุณากรอกรายละเอียดกิจกรรม';
        isValid = false;
    }
    if (!eventDate) {
        document.getElementById('dateError').textContent = 'กรุณาเลือกวันที่';
        isValid = false;
    }
    if (!eventTime) {
        document.getElementById('timeError').textContent = 'กรุณาเลือกเวลา';
        isValid = false;
    }

    // ตรวจสอบเรื่องวันและเวลาย้อนหลัง รวมถึงการตั้งเวลาล่วงหน้าไม่เกิน 7 วัน
    if (eventDate) {
        const now = new Date();
        const todayStr = formatDateYYYYMMDD(now);
        const maxDate = new Date(now);
        maxDate.setDate(now.getDate() + 7);
        const maxDateStr = formatDateYYYYMMDD(maxDate);

        if (eventDate < todayStr) {
            document.getElementById('dateError').textContent = 'ไม่สามารถสร้างกิจกรรมในอดีตได้';
            isValid = false;
        } else if (eventDate > maxDateStr) {
            document.getElementById('dateError').textContent = 'สามารถสร้างกิจกรรมล่วงหน้าได้ไม่เกิน 7 วัน';
            isValid = false;
        } else if (eventTime) {
            const selectedDateTime = new Date(`${eventDate}T${eventTime}`);
            if (selectedDateTime < now) {
                document.getElementById('timeError').textContent = 'ไม่สามารถเลือกเวลาย้อนหลังได้ กรุณาเลือกเวลาใหม่อีกครั้ง';
                isValid = false;
            }
        }
    }

    if (!isValid) {
        return; // หยุดการทำงานหากข้อมูลไม่ครบถ้วน
    }

    const submitBtn = document.querySelector('.btn-create');
    submitBtn.disabled = true;
    submitBtn.textContent = 'CREATING...';

    // 4. ดึงข้อมูลผู้ใช้
    let creatorStudentId = '6800000000';
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
        // 5. ส่งข้อมูลไปยัง Backend (ใช้ชื่อคีย์หลักตัวเดียวให้สะอาดเรียบร้อย)
        const apiBaseUrl = (window.location.protocol === 'file:' || window.location.port === '5500' || window.location.port === '5501')
            ? 'http://localhost:8080/api'
            : '/api';
        const response = await fetch(`${apiBaseUrl}/events`, { 
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                eventType: eventType,
                eventName: eventName,
                eventLocation: eventLocation,
                eventDescription: eventDescription,
                eventSize: parseInt(eventSize, 10),
                eventDate: eventDate,
                eventTime: eventTime,
                creatorStudentId: creatorStudentId
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'ไม่สามารถสร้างกิจกรรมได้ กรุณาลองใหม่อีกครั้ง');
        }

        // กรณีสำเร็จ: แจ้งเตือนและกลับหน้าหลัก
        alert('สร้างกิจกรรมสำเร็จ!');
        eventForm.reset(); 
        window.location.href = './Homepage.html'; 

    } catch (error) {
        const errorMessage = error.message || "Failed to create event, please try again.";
        if (errorBox) {
            errorBox.textContent = errorMessage;
            errorBox.classList.remove('hidden');
        } else {
            alert(errorMessage);
        }
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'CREATE';
    }
});

// เปิดปุ่มเลือกเวลาและปฏิทินอัตโนมัติเมื่อคลิก
if (timeInput) {
    timeInput.addEventListener('click', function() {
        if (typeof this.showPicker === 'function') this.showPicker();
    });
}

if (dateInput) {
    dateInput.addEventListener('click', function() {
        if (typeof this.showPicker === 'function') this.showPicker();
    });
}

// Custom Dropdown สำหรับ Size
const sizeSelect = document.getElementById('eventSize');
const sizeBtn = document.getElementById('sizeDropdownBtn');
const sizeMenu = document.getElementById('sizeDropdownMenu');
const sizeSelected = document.getElementById('sizeSelected');

if (sizeSelect && sizeBtn && sizeMenu) {

    // สร้างรายการจาก Select เดิม
    Array.from(sizeSelect.options).forEach(option => {

        const item = document.createElement('button');

        item.type = 'button';
        item.className = 'size-option';
        item.textContent = option.value;
        item.dataset.value = option.value;

        if (option.selected) {
            item.classList.add('selected');
        }

        item.addEventListener('click', () => {

            // เปลี่ยนค่าของ Select เดิม
            sizeSelect.value = option.value;

            // เปลี่ยนข้อความบนปุ่ม
            sizeSelected.textContent = option.value;

            // อัปเดตสถานะตัวเลือก
            sizeMenu.querySelectorAll('.size-option')
                .forEach(btn => btn.classList.remove('selected'));

            item.classList.add('selected');

            // ปิด Dropdown
            sizeMenu.classList.add('hidden');
        });

        sizeMenu.appendChild(item);
    });

    // เปิด / ปิด Dropdown
    sizeBtn.addEventListener('click', () => {
        sizeMenu.classList.toggle('hidden');
    });

    // คลิกพื้นที่อื่นเพื่อปิด
    document.addEventListener('click', (event) => {
        if (!event.target.closest('.size-select-wrapper')) {
            sizeMenu.classList.add('hidden');
        }
    });
}
