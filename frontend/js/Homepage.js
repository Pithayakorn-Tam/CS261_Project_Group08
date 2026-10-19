// ==========================================
// 1. ระบบ Carousel เลื่อนแบนเนอร์อัตโนมัติทุก 5 วินาที
// ==========================================
let currentSlide = 0;
const slides = document.querySelectorAll('.carousel-item');
const dots = document.querySelectorAll('.dot');
const carouselSlide = document.getElementById('carouselSlide');

function showSlide(index) {
    // เช็กให้อยู่ในลูป
    if (index >= slides.length) currentSlide = 0;
    if (index < 0) currentSlide = slides.length - 1;
    
    // เลื่อนรูป
    if(carouselSlide) {
        carouselSlide.style.transform = `translateX(-${currentSlide * 100}%)`;
    }
    
    // สลับจุด Active
    dots.forEach(dot => dot.classList.remove('active'));
    if(dots[currentSlide]) {
        dots[currentSlide].classList.add('active');
    }
}

// ตั้งเวลาให้เปลี่ยนรูปทุก 5000 มิลลิวินาที (5 วินาที)
setInterval(() => {
    currentSlide++;
    showSlide(currentSlide);
}, 5000);


// ==========================================
// 2. ระบบแสดงผลกิจกรรม 4 อันแรกเสมอ + กดปุ่มแสดงเพิ่มเติมเพื่อดึงจาก Database
// ==========================================

// ตารางจับคู่ชื่อหมวดหมู่ (ทั้งจาก DB และภาษาไทย) กับไฟล์รูปภาพ Logo
const categoryLogos = {
    'study': '../Picture/AcademicActivityLogo.svg',
    'วิชาการ': '../Picture/AcademicActivityLogo.svg',
    'sport': '../Picture/HealthActivityLogo.svg',
    'กีฬา': '../Picture/HealthActivityLogo.svg',
    'bordgame': '../Picture/EntertainmentAcivityLogo.svg',
    'E-sport': '../Picture/EntertainmentAcivityLogo.svg',
    'consert': '../Picture/EntertainmentAcivityLogo.svg',
    'บันเทิง': '../Picture/EntertainmentAcivityLogo.svg',
    'food': '../Picture/LifestyleActivityLogo.svg',
    'other': '../Picture/LifestyleActivityLogo.svg',
    'ไลฟ์สไตล์': '../Picture/LifestyleActivityLogo.svg',
    'default': '../Picture/LogoTUmeet.svg' 
};

// กิจกรรมตั้งต้น 4 อันแรกที่จะแสดงอยู่เสมอ
const defaultFourEvents = [
    {
        eventName: 'ตี๋น้อย',
        eventType: 'food',
        eventTime: '19.00',
        eventDate: '2026-10-10',
        location: 'Mingle, ม.กรุงเทพ',
        currentParticipants: 0,
        eventSize: 6
    },
    {
        eventName: 'อ่านหนังสือ',
        eventType: 'study',
        eventTime: '18.00',
        eventDate: '2026-10-12',
        location: 'ห้องสมุด',
        currentParticipants: 1,
        eventSize: 3
    },
    {
        eventName: 'ตีแบดมินตัน',
        eventType: 'sport',
        eventTime: '20.00',
        eventDate: '2026-10-13',
        location: 'ยิม มธ.',
        currentParticipants: 2,
        eventSize: 4
    },
    {
        eventName: 'บอร์ดเกม',
        eventType: 'bordgame',
        eventTime: '13.00',
        eventDate: '2026-10-18',
        location: 'Mingle, ม.กรุงเทพ',
        currentParticipants: 4,
        eventSize: 6
    }
];

// แปลงวันที่จาก YYYY-MM-DD (เช่น "2026-10-25") เป็นรูปแบบ "Sun, 25 Oct"
function formatEventDate(dateStr) {
    if (!dateStr) return '-';
    // ถ้าเป็นรูปแบบที่มีตัวอักษรอยู่แล้ว เช่น "Sat, 10 Oct" ให้คืนค่าเดิม
    if (/[a-zA-Z]/.test(dateStr)) return dateStr;

    const parts = dateStr.split('-');
    if (parts.length !== 3) return dateStr;

    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1;
    const day = parseInt(parts[2], 10);

    const dateObj = new Date(year, month, day);
    if (isNaN(dateObj.getTime())) return dateStr;

    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

    return `${days[dateObj.getDay()]}, ${day} ${months[dateObj.getMonth()]}`;
}

// ฟังก์ชันสร้าง HTML Card ของแต่ละกิจกรรม
function createEventCardElement(event) {
    const newCard = document.createElement('div');
    newCard.className = 'event-card';

    const typeKey = event.eventType || event.category || 'default';
    const logoSrc = categoryLogos[typeKey] || categoryLogos['default'];
    const title = event.eventName || event.title || '-';
    const time = event.eventTime || event.time || '-';
    const dateFormatted = formatEventDate(event.eventDate || event.date);
    const location = event.location || event.eventLocation || '-';
    const capacity = event.capacity
        ? event.capacity
        : `${event.currentParticipants ?? 0}/${event.eventSize ?? 1}`;

    newCard.innerHTML = `
        <div class="event-logo">
            <img src="${logoSrc}" alt="${typeKey} Logo">
        </div>
        <div class="event-info">
            <div class="event-badge">${title}</div>
            <div class="event-time-date">
                <span><img src="../Picture/TimeIcon.svg" class="detail-icon" alt="Time"> ${time}</span>
                <span><img src="../Picture/CalendarIcon.svg" class="detail-icon" alt="Calendar"> ${dateFormatted}</span>
            </div>
            <div class="event-location">
                <img src="../Picture/LocationIcon.svg" class="detail-icon" alt="Location"> ${location}
            </div>
        </div>
        <div class="event-action">
            <button class="join-btn">+</button>
            <div class="capacity">${capacity}</div>
        </div>
    `;
    return newCard;
}

const loadMoreBtn = document.getElementById('loadMoreBtn');
const eventList = document.getElementById('eventList');
let isExpanded = false;

// แสดง 4 กิจกรรมตั้งต้นเสมอตั้งแต่เปิดหน้าเว็บ
function renderInitialFourEvents() {
    if (!eventList) return;
    eventList.innerHTML = '';
    defaultFourEvents.forEach(ev => {
        eventList.appendChild(createEventCardElement(ev));
    });
}

// ตรวจสอบว่าเป็น 1 ใน 4 กิจกรรมตั้งต้นที่ถูก Seed ไว้ใน DB หรือไม่
function isDefaultSeededEvent(dbEvent, index) {
    if (index < 4) {
        const def = defaultFourEvents[index];
        if (def && dbEvent.eventName === def.eventName && dbEvent.eventType === def.eventType) {
            return true;
        }
    }
    return false;
}

renderInitialFourEvents();

if (loadMoreBtn && eventList) {
    loadMoreBtn.addEventListener('click', async () => {
        // กรณีกำลังแสดงทั้งหมดอยู่ เมื่อกดปุ่ม "แสดงน้อยลง" ให้กลับไปแสดงแค่ 4 อันแรก
        if (isExpanded) {
            renderInitialFourEvents();
            loadMoreBtn.textContent = 'แสดงเพิ่มเติม';
            isExpanded = false;
            return;
        }

        // กรณีกดปุ่ม "แสดงเพิ่มเติม" ให้ดึงกิจกรรมที่สร้างเพิ่มจากฐานข้อมูลมาแสดงต่อท้าย
        try {
            const response = await fetch('http://localhost:8080/api/events');
            if (!response.ok) {
                throw new Error('ไม่สามารถดึงข้อมูลกิจกรรมจากฐานข้อมูลได้');
            }

            const result = await response.json();
            const dbEvents = Array.isArray(result.data) ? result.data : [];

            // กรองเฉพาะกิจกรรมที่ถูกสร้างเพิ่มเติมในฐานข้อมูล (หลังจาก 4 อันแรก)
            const additionalEvents = dbEvents.filter((ev, idx) => !isDefaultSeededEvent(ev, idx));

            additionalEvents.forEach(ev => {
                eventList.appendChild(createEventCardElement(ev));
            });

            loadMoreBtn.textContent = 'แสดงน้อยลง';
            isExpanded = true;

        } catch (error) {
            console.error('Error loading events from database:', error);
        }
    });
}

// ===================================
// ปุ่มสร้างกิจกรรม
// ===================================
const createEventBtn = document.getElementById("createEventBtn");

if (createEventBtn) {
    createEventBtn.addEventListener("click", () => {
        window.location.href = "./CreateEvent.html";
    });
}

// ==========================================
// join popup
// ==========================================
async function loadJoinPopup() {
    const container = document.getElementById('joinPopupContainer');

    try {
        const response = await fetch('../html/Join.html');

        if (!response.ok) {
            throw new Error('ไม่สามารถโหลด Join.html ได้');
        }

        container.innerHTML = await response.text();

        // เริ่มทำงานของ Popup หลังโหลด HTML เสร็จ
        initJoinPopup();
    } catch (error) {
        console.error('โหลด Popup ไม่สำเร็จ:', error);
    }
}

function initJoinPopup() {
    // ผูกปุ่มเข้าร่วมกิจกรรม
    // แสดง Popup เมื่อเข้าร่วมสำเร็จหรือไม่สำเร็จ
    // ปิด Popup เมื่อกดปุ่มตกลง
}

loadJoinPopup();
