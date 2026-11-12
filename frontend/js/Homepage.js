// ==========================================
// 1. Carousel
// ==========================================

let currentSlide = 0;

const slides = document.querySelectorAll('.carousel-item');
const dots = document.querySelectorAll('.dot');
const carouselSlide = document.getElementById('carouselSlide');

function showSlide(index) {

    if (index >= slides.length) {
        currentSlide = 0;
    }

    if (index < 0) {
        currentSlide = slides.length - 1;
    }

    if (carouselSlide) {
        carouselSlide.style.transform =
            `translateX(-${currentSlide * 100}%)`;
    }

    dots.forEach(dot => {
        dot.classList.remove('active');
    });

    if (dots[currentSlide]) {
        dots[currentSlide].classList.add('active');
    }
}


if (slides.length > 0) {

    setInterval(() => {

        currentSlide++;

        showSlide(currentSlide);

    }, 5000);

}


// ==========================================
// 2. Mock Data
// ==========================================

const USE_MOCK_DATA = true;


const mockEvents = [

    {
        id: 1,
        eventName: 'ตีแบด',
        eventType: 'sport',
        eventTime: '09.00',
        eventDate: '2026-11-13',
        location: 'Mingle, ม.กรุงเทพ',
        currentParticipants: 1,
        eventSize: 6,
        status: 'OPEN',
        participants: []
    },

    {
        id: 2,
        eventName: 'วิ่งออกกำลังกาย',
        eventType: 'sport',
        eventTime: '10.00',
        eventDate: '2026-11-13',
        location: 'สนามกีฬา มธ',
        currentParticipants: 3,
        eventSize: 10,
        status: 'OPEN',
        participants: []
    },

    {
        id: 3,
        eventName: 'ติวสอบวิชา Programming',
        eventType: 'academic',
        eventTime: '13.00',
        eventDate: '2026-11-13',
        location: 'ห้อง SC101',
        currentParticipants: 4,
        eventSize: 8,
        status: 'OPEN',
        participants: []
    },

    {
        id: 4,
        eventName: 'เล่นเกมด้วยกัน',
        eventType: 'entertainment',
        eventTime: '18.00',
        eventDate: '2026-11-13',
        location: 'ร้านเกม NVIDIA',
        currentParticipants: 5,
        eventSize: 10,
        status: 'OPEN',
        participants: []
    },

    {
        id: 5,
        eventName: 'กินข้าวเย็น',
        eventType: 'life-style',
        eventTime: '18.30',
        eventDate: '2026-11-14',
        location: 'โรงอาหารกลาง มธ',
        currentParticipants: 2,
        eventSize: 6,
        status: 'OPEN',
        participants: []
    },

    {
        id: 6,
        eventName: 'ดูหนัง',
        eventType: 'entertainment',
        eventTime: '19.00',
        eventDate: '2026-11-14',
        location: 'Future Park',
        currentParticipants: 4,
        eventSize: 8,
        status: 'OPEN',
        participants: []
    },

    {
        id: 7,
        eventName: 'อ่านหนังสือสอบ',
        eventType: 'academic',
        eventTime: '20.00',
        eventDate: '2026-11-14',
        location: 'หอสมุดป๋วย',
        currentParticipants: 2,
        eventSize: 5,
        status: 'OPEN',
        participants: []
    },

    {
        id: 8,
        eventName: 'เตะฟุตบอล',
        eventType: 'sport',
        eventTime: '16.00',
        eventDate: '2026-11-15',
        location: 'สนามฟุตบอล มธ',
        currentParticipants: 7,
        eventSize: 10,
        status: 'OPEN',
        participants: []
    },

    {
        id: 9,
        eventName: 'ดูคอนเสิร์ต',
        eventType: 'entertainment',
        eventTime: '18.00',
        eventDate: '2026-11-15',
        location: 'อาคารยิมเนเซียม',
        currentParticipants: 6,
        eventSize: 10,
        status: 'OPEN',
        participants: []
    },

    {
        id: 10,
        eventName: 'คาเฟ่และพูดคุย',
        eventType: 'life-style',
        eventTime: '19.30',
        eventDate: '2026-11-15',
        location: 'คาเฟ่ใกล้มหาวิทยาลัย',
        currentParticipants: 3,
        eventSize: 6,
        status: 'OPEN',
        participants: []
    },

    {
        id: 11,
        eventName: 'กิจกรรมอื่นๆ',
        eventType: 'other',
        eventTime: '17.00',
        eventDate: '2026-11-15',
        location: 'มหาวิทยาลัย',
        currentParticipants: 2,
        eventSize: 5,
        status: 'OPEN',
        participants: []
    }

];


// ==========================================
// 3. Category Logos
// ==========================================

const categoryLogos = {

    // วิชาการ
    academic:
        '../Picture/book-one.svg',

    // กีฬา
    sport:
        '../Picture/activity.svg',

    // บันเทิง
    entertainment:
        '../Picture/gamecontroller.svg',

    // ไลฟ์สไตล์
    'life-style':
        '../Picture/bowl.svg',

    // อื่นๆ
    other:
        '../Picture/bowl.svg',

    // รูปสำรอง
    default:
        '../Picture/bowl.svg'

};


// ==========================================
// 4. Category Filter
// ==========================================

const filterCategoryMap = {

    academic: [
        'academic'
    ],

    sport: [
        'sport'
    ],

    entertainment: [
        'entertainment'
    ],

    'life-style': [
        'life-style'
    ],

    other: [
        'other'
    ]

};


// ==========================================
// 5. Current Student
// ==========================================

function getCurrentStudentId() {

    try {

        const storedLogin =
            JSON.parse(
                localStorage.getItem('loginData') || '{}'
            );


        if (storedLogin.studentId) {
            return storedLogin.studentId;
        }


        if (localStorage.getItem('studentId')) {
            return localStorage.getItem('studentId');
        }

    } catch (error) {

        console.warn(
            'Cannot read login data:',
            error
        );

    }


    return '6800000000';

}


// ==========================================
// 6. Format Date
// ==========================================

function formatEventDate(dateStr) {

    if (!dateStr) {
        return '-';
    }


    if (/[a-zA-Z]/.test(dateStr)) {
        return dateStr;
    }


    const parts =
        String(dateStr).split('-');


    if (parts.length !== 3) {
        return dateStr;
    }


    const year =
        parseInt(parts[0], 10);

    const month =
        parseInt(parts[1], 10) - 1;

    const day =
        parseInt(parts[2], 10);


    const dateObj =
        new Date(
            year,
            month,
            day
        );


    if (isNaN(dateObj.getTime())) {
        return dateStr;
    }


    const days = [
        'Sun',
        'Mon',
        'Tue',
        'Wed',
        'Thu',
        'Fri',
        'Sat'
    ];


    const months = [
        'Jan',
        'Feb',
        'Mar',
        'Apr',
        'May',
        'Jun',
        'Jul',
        'Aug',
        'Sep',
        'Oct',
        'Nov',
        'Dec'
    ];


    return `${days[dateObj.getDay()]}, ${day} ${months[dateObj.getMonth()]}`;

}


// ==========================================
// 7. Event Timestamp
// ==========================================

function getEventTimestamp(event) {

    if (!event) {
        return 0;
    }


    const dateStr =
        event.eventDate ||
        event.date;


    if (!dateStr) {
        return 0;
    }


    const parts =
        String(dateStr).split('-');


    if (parts.length !== 3) {
        return 0;
    }


    const year =
        parseInt(parts[0], 10);

    const month =
        parseInt(parts[1], 10) - 1;

    const day =
        parseInt(parts[2], 10);


    let hours = 23;
    let minutes = 59;


    const timeStr =
        String(
            event.eventTime ||
            event.time ||
            ''
        ).trim();


    if (timeStr) {

        const timeParts =
            timeStr
                .replace('.', ':')
                .split(':');


        if (timeParts.length >= 2) {

            const h =
                parseInt(
                    timeParts[0],
                    10
                );

            const m =
                parseInt(
                    timeParts[1],
                    10
                );


            if (
                !isNaN(h) &&
                !isNaN(m)
            ) {

                hours = h;
                minutes = m;

            }

        }

    }


    const eventDateTime =
        new Date(
            year,
            month,
            day,
            hours,
            minutes,
            59
        );


    return isNaN(
        eventDateTime.getTime()
    )
        ? 0
        : eventDateTime.getTime();

}


// ==========================================
// 8. Check Expired Event
// ==========================================

function isEventExpired(event) {

    if (!event) {
        return false;
    }


    if (
        event.status === 'CLOSED' ||
        event.status === 'CANCELLED'
    ) {

        return true;

    }


    const timestamp =
        getEventTimestamp(event);


    if (timestamp === 0) {
        return false;
    }


    return timestamp < Date.now();

}


// ==========================================
// 9. Sort Events
// ==========================================

function sortEvents(events) {

    return [...events].sort(
        (a, b) => {

            const expiredA =
                isEventExpired(a);

            const expiredB =
                isEventExpired(b);


            if (
                expiredA !== expiredB
            ) {

                return expiredA
                    ? 1
                    : -1;

            }


            const timeA =
                getEventTimestamp(a);

            const timeB =
                getEventTimestamp(b);


            if (
                !expiredA &&
                !expiredB
            ) {

                return timeA - timeB;

            }


            return timeB - timeA;

        }
    );

}


// ==========================================
// 10. Match Category
// ==========================================

function matchesCategoryFilter(
    event,
    filterValue
) {

    if (
        !filterValue ||
        filterValue === 'all'
    ) {

        return true;

    }


    const allowedTypes =
        filterCategoryMap[
            filterValue
        ];


    if (!allowedTypes) {
        return true;
    }


    const typeKey =
        String(
            event.eventType ||
            event.category ||
            ''
        ).trim();


    return allowedTypes.includes(
        typeKey
    );

}


// ==========================================
// 11. Create Event Card
// ==========================================

function createEventCardElement(event) {

    const expired =
        isEventExpired(event);


    const newCard =
        document.createElement('div');


    newCard.className =
        expired
            ? 'event-card expired'
            : 'event-card';


    // --------------------------------------
    // Event Data
    // --------------------------------------

    const typeKey =
        event.eventType ||
        event.category ||
        'default';


    const logoSrc =
        categoryLogos[typeKey] ||
        categoryLogos.default;


    const title =
        event.eventName ||
        event.title ||
        '-';


    const time =
        event.eventTime ||
        event.time ||
        '-';


    const dateFormatted =
        formatEventDate(
            event.eventDate ||
            event.date
        );


    const location =
        event.location ||
        event.eventLocation ||
        '-';


    // --------------------------------------
    // Participants
    // --------------------------------------

    const currentParticipants =
        event.currentParticipants ?? 1;


    const eventSize =
        event.eventSize ?? 1;


    const capacityText =
        event.capacity
            ? event.capacity
            : `${currentParticipants}/${eventSize}`;


    // --------------------------------------
    // Event ID
    // --------------------------------------

    if (event.id != null) {

        newCard.dataset.eventId =
            String(event.id);

    }


    // --------------------------------------
    // Current Student
    // --------------------------------------

    const currentStudentId =
        getCurrentStudentId();


    const isAlreadyJoined =
        Array.isArray(
            event.participants
        ) &&
        event.participants.some(
            participant =>
                participant &&
                participant.studentId ===
                currentStudentId
        );


    const isCreator =
        Boolean(
            event.creator &&
            event.creator.studentId ===
            currentStudentId
        );


    const isJoinedOrCreator =
        isAlreadyJoined ||
        isCreator;


    // ======================================
    // Join Button
    // ======================================

    let actionButtonHtml = '';


    if (!expired) {

        if (isJoinedOrCreator) {

            actionButtonHtml = `

                <button
                    class="join-btn"
                    disabled
                    data-joined="true"
                    aria-label="เข้าร่วมกิจกรรมแล้ว"
                >

                    <span class="joined-check">
                        ✓
                    </span>

                </button>

            `;

        } else {

            actionButtonHtml = `

                <button
                    class="join-btn"
                    aria-label="เข้าร่วมกิจกรรม"
                >

                    <img
                        src="../Picture/plus-mark.svg"
                        alt="Join"
                    >

                </button>

            `;

        }

    }


    // ======================================
    // Event Card HTML
    // ======================================

    newCard.innerHTML = `

        <div class="event-logo">

            <img
                src="${logoSrc}"
                alt="${typeKey}"
            >

        </div>


        <div class="event-info">

            <!-- Event Name + Info -->

            <div class="event-badge">

                <span class="event-title">
                    ${title}
                </span>


                <button
                    class="event-info-btn"
                    type="button"
                    data-event-id="${event.id}"
                    aria-label="ดูรายละเอียดกิจกรรม"
                >

                    <img
                        src="../Picture/Info.svg"
                        alt="Info"
                    >

                </button>

            </div>


            <!-- Time + Date -->

            <div class="event-time-date">

                <span>

                    <img
                        src="../Picture/TimeIcon.svg"
                        class="detail-icon"
                        alt="Time"
                    >

                    ${time}

                </span>


                <span>

                    <img
                        src="../Picture/CalendarIcon.svg"
                        class="detail-icon"
                        alt="Calendar"
                    >

                    ${dateFormatted}

                </span>

            </div>


            <!-- Location -->

            <div class="event-location">

                <img
                    src="../Picture/LocationIcon.svg"
                    class="detail-icon"
                    alt="Location"
                >

                ${location}

            </div>

        </div>


        <!-- Join + Participants -->

        <div class="event-action">

            ${actionButtonHtml}


            <div class="capacity">

                <img
                    src="../Picture/people-outline.svg"
                    class="people-icon"
                    alt="Participants"
                >

                <span>
                    ${capacityText}
                </span>

            </div>

        </div>

    `;


    // ======================================
    // Info Button
    // ======================================

    const infoButton =
        newCard.querySelector(
            '.event-info-btn'
        );


    if (infoButton) {

        infoButton.addEventListener(
            'click',
            (event) => {

                event.stopPropagation();


                const eventId =
                    infoButton.dataset.eventId;


                console.log(
                    'Info clicked. Event ID:',
                    eventId
                );

                // ยังไม่เปิดหน้ารายละเอียด
                // เพราะยังไม่ได้สร้างหน้ารายละเอียดกิจกรรม

            }
        );

    }


    return newCard;

}


// ==========================================
// 12. Event List
// ==========================================

const loadMoreBtn =
    document.getElementById(
        'loadMoreBtn'
    );


const eventList =
    document.getElementById(
        'eventList'
    );


const filterDropdown =
    document.querySelector(
        '.filter-dropdown'
    );


let isExpanded = false;

let allFetchedEvents = [];


// ==========================================
// 13. Fetch Events
// ==========================================

async function fetchEventsFromBackend() {

    // --------------------------------------
    // Mock Data
    // --------------------------------------

    if (USE_MOCK_DATA) {

        allFetchedEvents =
            [...mockEvents];

        return allFetchedEvents;

    }


    // --------------------------------------
    // Backend
    // --------------------------------------

    try {

        const apiBaseUrl =
            (
                window.location.protocol === 'file:' ||
                window.location.port === '5500' ||
                window.location.port === '5501'
            )
                ? 'http://localhost:8080/api'
                : '/api';


        const response =
            await fetch(
                `${apiBaseUrl}/events`
            );


        if (!response.ok) {

            throw new Error(
                'ไม่สามารถดึงข้อมูลกิจกรรมจากฐานข้อมูลได้'
            );

        }


        const result =
            await response.json();


        allFetchedEvents =
            Array.isArray(
                result.data
            )
                ? result.data
                : [];


        return allFetchedEvents;

    } catch (error) {

        console.error(
            'Error fetching events:',
            error
        );


        allFetchedEvents = [];


        return allFetchedEvents;

    }

}


// ==========================================
// 14. Render Events
// ==========================================

function renderEventsOnHome(
    eventsToRender
) {

    if (!eventList) {
        return;
    }


    eventList.innerHTML = '';


    if (
        eventsToRender.length === 0
    ) {

        eventList.innerHTML = `

            <div class="empty-message">

                ไม่พบข้อมูลกิจกรรมในหมวดหมู่นี้

            </div>

        `;

        return;

    }


    eventsToRender.forEach(
        event => {

            eventList.appendChild(
                createEventCardElement(
                    event
                )
            );

        }
    );

}


// ==========================================
// 15. Filter + Sort
// ==========================================

function getFilteredAndSortedEvents() {

    const selectedCategory =
        filterDropdown
            ? filterDropdown.value
            : 'all';


    const filtered =
        allFetchedEvents.filter(
            event =>
                matchesCategoryFilter(
                    event,
                    selectedCategory
                )
        );


    return sortEvents(
        filtered
    );

}


// ==========================================
// 16. Update Home Events
// ==========================================

function updateHomeEventsView() {

    const sortedEvents =
        getFilteredAndSortedEvents();


    // --------------------------------------
    // แสดง 6 รายการแรก
    // --------------------------------------

    if (isExpanded) {

        // แสดงกิจกรรมทั้งหมด
        renderEventsOnHome(
            sortedEvents
        );

    } else {

        // แสดง 6 กิจกรรมแรก
        renderEventsOnHome(
            sortedEvents.slice(0, 6)
        );

    }


    // --------------------------------------
    // Load More Button
    // --------------------------------------

    if (loadMoreBtn) {

        if (
            sortedEvents.length > 6
        ) {

            loadMoreBtn.style.display =
                'block';


            loadMoreBtn.textContent =
                isExpanded
                    ? 'แสดงข้อมูลให้น้อยลง'
                    : 'แสดงเพิ่มเติม';

        } else {

            loadMoreBtn.style.display =
                'none';

        }

    }

}


// ==========================================
// 17. Initial Events
// ==========================================

async function initHomeEvents() {

    await fetchEventsFromBackend();

    isExpanded = false;

    updateHomeEventsView();

}


initHomeEvents();


// ==========================================
// 18. Load More
// ==========================================

if (
    loadMoreBtn &&
    eventList
) {

    loadMoreBtn.addEventListener(
        'click',
        async () => {

            await fetchEventsFromBackend();


            isExpanded =
                !isExpanded;


            updateHomeEventsView();

        }
    );

}


// ==========================================
// 19. Filter
// ==========================================

if (filterDropdown) {

    filterDropdown.addEventListener(
        'change',
        async () => {

            await fetchEventsFromBackend();

            isExpanded = false;

            updateHomeEventsView();

        }
    );

}


// ==========================================
// 20. Create Event Button
// ==========================================

const createEventBtn =
    document.getElementById(
        'createEventBtn'
    );


if (createEventBtn) {

    createEventBtn.addEventListener(
        'click',
        () => {

            window.location.href =
                './CreateEvent.html';

        }
    );

}


// ==========================================
// 21. Join Popup
// ==========================================

async function loadJoinPopup() {

    const container =
        document.getElementById(
            'joinPopupContainer'
        );


    if (!container) {
        return;
    }


    try {

        const response =
            await fetch(
                '../html/Join.html'
            );


        if (!response.ok) {

            throw new Error(
                'ไม่สามารถโหลด Join.html ได้'
            );

        }


        container.innerHTML =
            await response.text();


        if (
            typeof initJoinPopup ===
            'function'
        ) {

            initJoinPopup();

        }

    } catch (error) {

        console.error(
            'โหลด Popup ไม่สำเร็จ:',
            error
        );

    }

}


loadJoinPopup();