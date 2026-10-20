// ==========================================
// 1. ระบบ Carousel เลื่อนแบนเนอร์อัตโนมัติทุก 5 วินาที
// ==========================================
let currentSlide = 0;
const slides = document.querySelectorAll('.carousel-item');
const dots = document.querySelectorAll('.dot');
const carouselSlide = document.getElementById('carouselSlide');

function showSlide(index) {
    if (index >= slides.length) currentSlide = 0;
    if (index < 0) currentSlide = slides.length - 1;

    if (carouselSlide) {
        carouselSlide.style.transform = `translateX(-${currentSlide * 100}%)`;
    }

    dots.forEach(dot => dot.classList.remove('active'));
    if (dots[currentSlide]) {
        dots[currentSlide].classList.add('active');
    }
}

setInterval(() => {
    currentSlide++;
    showSlide(currentSlide);
}, 5000);

// ==========================================
// 2. ระบบดึงข้อมูลกิจกรรมจากฐานข้อมูล + เรียงลำดับ + กรองหมวดหมู่ (Now Showing)
// ==========================================

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

// ตารางจับคู่ตัวกรอง (Now Showing) กับ eventType ในฐานข้อมูล
const filterCategoryMap = {
    'academic': ['study', 'วิชาการ', 'academic'],
    'sport': ['sport', 'กีฬา'],
    'entertainment': ['bordgame', 'E-sport', 'consert', 'บันเทิง', 'entertainment'],
    'lifestyle': ['food', 'other', 'ไลฟ์สไตล์', 'lifestyle']
};

function getCurrentStudentId() {
    try {
        const storedLogin = JSON.parse(localStorage.getItem('loginData') || '{}');
        if (storedLogin.studentId) return storedLogin.studentId;
        if (localStorage.getItem('studentId')) return localStorage.getItem('studentId');
    } catch (e) {
        console.warn('Cannot read login data:', e);
    }
    return '6800000000';
}

function formatEventDate(dateStr) {
    if (!dateStr) return '-';
    if (/[a-zA-Z]/.test(dateStr)) return dateStr;

    const parts = String(dateStr).split('-');
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

// แปลงวันและเวลาของกิจกรรมเป็น Timestamp (มิลลิวินาที)
function getEventTimestamp(event) {
    if (!event) return 0;
    const dateStr = event.eventDate || event.date;
    if (!dateStr) return 0;

    const parts = String(dateStr).split('-');
    if (parts.length !== 3) return 0;

    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1;
    const day = parseInt(parts[2], 10);

    let hours = 23;
    let minutes = 59;
    const timeStr = String(event.eventTime || event.time || '').trim();
    if (timeStr) {
        const timeParts = timeStr.replace('.', ':').split(':');
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
function isEventExpired(event) {
    if (!event) return false;
    if (event.status === 'CLOSED' || event.status === 'CANCELLED') {
        return true;
    }
    const ts = getEventTimestamp(event);
    if (ts === 0) return false;
    return ts < Date.now();
}

// เรียงลำดับกิจกรรม:
// 1. กิจกรรมที่ยังไม่หมดเวลาอยู่ด้านบน กิจกรรมที่ผ่านไปแล้ว (Expired) อยู่ด้านล่างสุด
// 2. ในกลุ่มที่ยังไม่หมดเวลา: กิจกรรมที่เพิ่งสร้างใหม่ (id > 4) อยู่ด้านบนสุด (เรียงตาม id ล่าสุด) ตามด้วยเรียงตามวันจัดกิจกรรม
// 3. ในกลุ่มที่หมดเวลาแล้ว: เรียงจากวันที่เพิ่งผ่านไปลงไปหาอันที่เก่าที่สุด
function sortEvents(events) {
    return [...events].sort((a, b) => {
        const expiredA = isEventExpired(a);
        const expiredB = isEventExpired(b);

        if (expiredA !== expiredB) {
            return expiredA ? 1 : -1;
        }

        const timeA = getEventTimestamp(a);
        const timeB = getEventTimestamp(b);

        if (!expiredA && !expiredB) {
            const isUserCreatedA = (Number(a.id) || 0) > 4;
            const isUserCreatedB = (Number(b.id) || 0) > 4;

            if (isUserCreatedA !== isUserCreatedB) {
                return isUserCreatedA ? -1 : 1;
            }
            if (isUserCreatedA && isUserCreatedB) {
                const idDiff = (Number(b.id) || 0) - (Number(a.id) || 0);
                if (idDiff !== 0) return idDiff;
            }
            return timeA - timeB;
        }

        // สำหรับกิจกรรมที่ผ่านไปแล้ว: อันที่เพิ่งผ่านไปอยู่บน อันเก่าสุดอยู่ล่างสุด
        return timeB - timeA;
    });
}

// ตรวจสอบว่ากิจกรรมตรงกับหมวดหมู่ที่เลือกใน Now Showing หรือไม่
function matchesCategoryFilter(event, filterValue) {
    if (!filterValue || filterValue === 'all') return true;
    const allowedTypes = filterCategoryMap[filterValue];
    if (!allowedTypes) return true;
    const typeKey = String(event.eventType || event.category || '').trim();
    return allowedTypes.includes(typeKey);
}

function createEventCardElement(event) {
    const expired = isEventExpired(event);

    const newCard = document.createElement('div');
    newCard.className = expired ? 'event-card expired' : 'event-card';

    const typeKey = event.eventType || event.category || 'default';
    const logoSrc = categoryLogos[typeKey] || categoryLogos['default'];
    const title = event.eventName || event.title || '-';
    const time = event.eventTime || event.time || '-';
    const dateFormatted = formatEventDate(event.eventDate || event.date);
    const location = event.location || event.eventLocation || '-';

    const currentParticipants = event.currentParticipants ?? 0;
    const eventSize = event.eventSize ?? 1;
    const capacityText = event.capacity ? event.capacity : `${currentParticipants}/${eventSize}`;

    if (event.id != null) {
        newCard.dataset.eventId = String(event.id);
    }
    newCard.dataset.participants = String(currentParticipants);
    newCard.dataset.capacity = String(eventSize);
    newCard.dataset.open = expired ? 'false' : 'true';
    newCard.dataset.cancelled = (event.status === 'CANCELLED') ? 'true' : 'false';

    const currentStudentId = getCurrentStudentId();
    const isAlreadyJoined = Array.isArray(event.participants) &&
        event.participants.some(p => p && p.studentId === currentStudentId);

    // ถ้ากิจกรรมหมดเวลาแล้ว: ไม่แสดงปุ่มกด (+) หรือเครื่องหมายใดๆ ทั้งสิ้น
    let actionButtonHtml = '';
    if (!expired) {
        const btnText = isAlreadyJoined ? '✓' : '+';
        const btnJoinedAttr = isAlreadyJoined ? 'data-joined="true" disabled' : '';
        actionButtonHtml = `<button class="join-btn" ${btnJoinedAttr}>${btnText}</button>`;
    }

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
            ${actionButtonHtml}
            <div class="capacity">${capacityText}</div>
        </div>
    `;
    return newCard;
}

const loadMoreBtn = document.getElementById('loadMoreBtn');
const eventList = document.getElementById('eventList');
const filterDropdown = document.querySelector('.filter-dropdown');
let isExpanded = false;
let allFetchedEvents = [];

async function fetchEventsFromBackend() {
    try {
        const response = await fetch('http://localhost:8080/api/events');
        if (!response.ok) {
            throw new Error('ไม่สามารถดึงข้อมูลกิจกรรมจากฐานข้อมูลได้');
        }
        const result = await response.json();
        allFetchedEvents = Array.isArray(result.data) ? result.data : [];
        return allFetchedEvents;
    } catch (error) {
        console.error('Error fetching events from backend:', error);
        allFetchedEvents = [];
        return allFetchedEvents;
    }
}

function renderEventsOnHome(eventsToRender) {
    if (!eventList) return;
    eventList.innerHTML = '';

    if (eventsToRender.length === 0) {
        eventList.innerHTML = '<div style="text-align:center; color:#888; padding: 16px 0; font-size: 14px;">ไม่พบข้อมูลกิจกรรมในหมวดหมู่นี้</div>';
        return;
    }

    eventsToRender.forEach(ev => {
        eventList.appendChild(createEventCardElement(ev));
    });
}

function getFilteredAndSortedEvents() {
    const selectedCategory = filterDropdown ? filterDropdown.value : 'all';
    const filtered = allFetchedEvents.filter(ev => matchesCategoryFilter(ev, selectedCategory));
    return sortEvents(filtered);
}

function updateHomeEventsView() {
    const sortedEvents = getFilteredAndSortedEvents();

    if (isExpanded) {
        renderEventsOnHome(sortedEvents);
    } else {
        renderEventsOnHome(sortedEvents.slice(0, 4));
    }

    if (loadMoreBtn) {
        if (sortedEvents.length > 4) {
            loadMoreBtn.style.display = 'block';
            loadMoreBtn.textContent = isExpanded ? 'แสดงข้อมูลให้น้อยลง' : 'แสดงเพิ่มเติม';
        } else {
            loadMoreBtn.style.display = 'none';
        }
    }
}

async function initHomeEvents() {
    await fetchEventsFromBackend();
    isExpanded = false;
    updateHomeEventsView();
}

initHomeEvents();

if (loadMoreBtn && eventList) {
    loadMoreBtn.addEventListener('click', async () => {
        await fetchEventsFromBackend();
        isExpanded = !isExpanded;
        updateHomeEventsView();
    });
}

if (filterDropdown) {
    filterDropdown.addEventListener('change', async () => {
        await fetchEventsFromBackend();
        isExpanded = false;
        updateHomeEventsView();
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

        if (typeof initJoinPopup === 'function') {
            initJoinPopup();
        }
    } catch (error) {
        console.error('โหลด Popup ไม่สำเร็จ:', error);
    }
}

loadJoinPopup();
