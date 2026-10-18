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
// 2. ระบบกดปุ่มแสดงเพิ่มเติม และจับคู่โลโก้หมวดหมู่
// ==========================================

// ตารางจับคู่ชื่อหมวดหมู่กับไฟล์รูปภาพ Logo
const categoryLogos = {
    'วิชาการ': '../Picture/AcademicActivityLogo.svg',
    'บันเทิง': '../Picture/EntertainmentActivityLogo.svg',
    'กีฬา': '../Picture/HealthActivityLogo.svg',
    'ไลฟ์สไตล์': '../Picture/LifestyleActivityLogo.svg',
    'default': '../Picture/LogoTUmeet.svg' 
};

const loadMoreBtn = document.getElementById('loadMoreBtn');
const eventList = document.getElementById('eventList');

if (loadMoreBtn && eventList) {
    loadMoreBtn.addEventListener('click', () => {
        
        // ข้อมูลจำลอง (Mock Data) 
        const newEvents = [
            { 
                title: 'อ่านหนังสือ', 
                category: 'วิชาการ', 
                time: '18.00', 
                date: 'Mon, 12 Oct', 
                location: 'ห้องสมุด', 
                capacity: '1/3' 
            },
            { 
                title: 'ตีแบดมินตัน', 
                category: 'กีฬา', 
                time: '20.00', 
                date: 'Tue, 13 Oct', 
                location: 'ยิม มธ.', 
                capacity: '2/4' 
            },
            { 
                title: 'บอร์ดเกม', 
                category: 'บันเทิง', 
                time: '13.00', 
                date: 'Sun, 18 Oct', 
                location: 'Mingle, ม.กรุงเทพ', 
                capacity: '4/6' 
            }
        ];

        // วนลูปสร้างการ์ดจากข้อมูลจำลอง
        newEvents.forEach(event => {
            const newCard = document.createElement('div');
            newCard.className = 'event-card';

            // เช็กโลโก้ตามหมวดหมู่ ถ้าไม่มีให้ใช้ LogoTUmeet
            const logoSrc = categoryLogos[event.category] || categoryLogos['default'];

            // โครงสร้าง HTML ของการ์ด (ใช้ SVG Icon ตามดีไซน์)
            newCard.innerHTML = `
                <div class="event-logo">
                    <img src="${logoSrc}" alt="${event.category} Logo">
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
                <div class="event-action">
                    <button class="join-btn">+</button>
                    <div class="capacity">${event.capacity}</div>
                </div>
            `;
            
            // เอาการ์ดใหม่ไปต่อท้ายในกล่อง eventList
            eventList.appendChild(newCard);
        });
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
