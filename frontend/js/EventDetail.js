// ============================================================================
// EventDetail.js - จัดการ Popup รายละเอียดกิจกรรม (รองรับทั้ง Mobile และ PC)
// ============================================================================

// ตั้งค่าการแสดงผลข้อมูลในการ์ดรายละเอียด:
// true  = แสดงข้อมูลตรงตามภาพตัวอย่างเป๊ะๆ (ตี๋น้อย คลายเหงา, 18.30, Sat, 28 Sep, หอสมุดป๋วย, 3/6)
// false = ดึงชื่อกิจกรรม ไอคอน วัน เวลา สถานที่ ตามการ์ดที่คลิกจริง
const USE_EXACT_IMAGE_MOCK = false;

(function () {
  const API_BASE_URL =
    window.location.protocol === 'file:' ||
    window.location.port === '5500' ||
    window.location.port === '5501'
      ? 'http://localhost:8080/api'
      : '/api';

  const DEFAULT_DESCRIPTION_PLACEHOLDER = 'กิจกรรมนี้ไม่มีคำอธิบายเพิ่มเติม';

  // ไอคอนแรงค์ของผู้เข้าร่วมตามลำดับในภาพต้นแบบ
  const RANK_BADGE_ICONS = [
    '../Picture/badge/Newly-Bee.svg',
    '../Picture/badge/Baby-Bloom.svg',
    '../Picture/badge/Happy-Blossom.svg',
    '../Picture/badge/Super-Bouquet.svg'
  ];

  // ข้อมูลต้นแบบตรงตามภาพ (ตี๋น้อย คลายเหงา)
  const MOCK_TEMPLATE_EVENT = {
    id: 'mock-teenoi',
    eventName: 'ตี๋น้อย คลายเหงา',
    eventType: 'life-style',
    logo: '../Picture/bowl.svg',
    description: '',
    eventTime: '18.30',
    eventDate: 'Sat, 28 Sep',
    location: 'หอสมุดป๋วย',
    currentParticipants: 3,
    eventSize: 6
  };

  // HTML สำรองกรณีเปิดผ่าน file:// แล้ว fetch ไม่ได้
  const FALLBACK_POPUP_HTML = `
  <div id="eventDetailPopup" class="event-detail-overlay hidden" role="dialog" aria-modal="true"
    aria-labelledby="eventDetailTitle">
    <div class="event-detail-card">
      <button type="button" id="eventDetailCloseBtn" class="event-detail-close-btn" aria-label="ปิดหน้าต่าง">✕</button>

      <div class="event-detail-header">
        <div class="event-detail-logo-wrap">
          <img id="eventDetailLogo" src="../Picture/bowl.svg" alt="Category Icon" class="event-detail-logo">
        </div>
        <h2 id="eventDetailTitle" class="event-detail-title">ตี๋น้อย คลายเหงา</h2>
      </div>

      <div class="event-detail-divider top-divider"></div>

      <div class="event-detail-body">
        <div class="event-detail-left">
          <div class="event-detail-desc-box" id="eventDetailDescBox">
            <div id="eventDetailDescText" class="event-detail-desc-text empty">กิจกรรมนี้ไม่มีคำอธิบายเพิ่มเติม</div>
            <textarea id="eventDetailDescInput" class="event-detail-desc-input hidden" maxlength="200"
              placeholder="กิจกรรมนี้ไม่มีคำอธิบายเพิ่มเติม"></textarea>
            <div id="eventDetailDescFooter" class="event-detail-desc-footer hidden">
              <span id="eventDetailCharCount" class="event-detail-char-count">0/200</span>
              <button type="button" id="eventDetailEditBtn" class="event-detail-edit-btn" aria-label="แก้ไขคำอธิบาย">
                <svg class="pencil-icon" viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor"
                  stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"></path>
                </svg>
              </button>
            </div>
          </div>

          <div class="event-detail-info-grid">
            <div class="event-detail-info-item">
              <img src="../Picture/TimeIcon.svg" alt="Time" class="event-detail-info-icon">
              <span id="eventDetailTime">18.30</span>
            </div>
            <div class="event-detail-info-item">
              <img src="../Picture/CalendarIcon.svg" alt="Date" class="event-detail-info-icon">
              <span id="eventDetailDate">Sat, 28 Sep</span>
            </div>
            <div class="event-detail-info-item">
              <img src="../Picture/LocationIcon.svg" alt="Location" class="event-detail-info-icon">
              <span id="eventDetailLocation">หอสมุดป๋วย</span>
            </div>
            <div class="event-detail-info-item">
              <img src="../Picture/people-outline.svg" alt="Participants" class="event-detail-info-icon people-icon-detail">
              <span id="eventDetailCapacity">3/6</span>
            </div>
          </div>
        </div>

        <div class="event-detail-divider mobile-middle-divider"></div>

        <div class="event-detail-right">
          <h3 id="eventDetailParticipantHeader" class="event-detail-participant-header">ผู้เข้าร่วม ( 3 )</h3>
          <div id="eventDetailParticipantList" class="event-detail-participant-list"></div>
        </div>
      </div>

      <div class="event-detail-footer" id="eventDetailFooter">
        <button type="button" id="eventDetailActionBtn" class="event-detail-action-btn btn-join">เข้าร่วม</button>
      </div>
    </div>
  </div>`;

  let currentModalState = {
    eventId: 'mock-teenoi',
    eventName: MOCK_TEMPLATE_EVENT.eventName,
    logo: MOCK_TEMPLATE_EVENT.logo,
    description: '',
    time: MOCK_TEMPLATE_EVENT.eventTime,
    date: MOCK_TEMPLATE_EVENT.eventDate,
    location: MOCK_TEMPLATE_EVENT.location,
    currentParticipants: 3,
    eventSize: 6,
    mode: 'other-not-joined', // 'other-not-joined' | 'other-joined' | 'created-by-me'
    sourceCard: null,
    isEditingDesc: false
  };

  function getCurrentStudentId() {
    try {
      const storedLogin = JSON.parse(localStorage.getItem('loginData') || '{}');
      if (storedLogin.studentId) return String(storedLogin.studentId);
      if (localStorage.getItem('studentId')) return String(localStorage.getItem('studentId'));
    } catch (e) {
      // ignore
    }
    return '6800000000';
  }

  function getSavedDescription(eventId, fallbackDesc = '') {
    try {
      const map = JSON.parse(localStorage.getItem('tumeet_event_descriptions') || '{}');
      if (eventId != null && Object.prototype.hasOwnProperty.call(map, String(eventId))) {
        return map[String(eventId)];
      }
    } catch (e) {
      // ignore
    }
    return fallbackDesc || '';
  }

  function saveDescription(eventId, desc) {
    try {
      const map = JSON.parse(localStorage.getItem('tumeet_event_descriptions') || '{}');
      map[String(eventId || 'default')] = desc;
      localStorage.setItem('tumeet_event_descriptions', JSON.stringify(map));
    } catch (e) {
      // ignore
    }
  }

  // สร้างรายชื่อผู้เข้าร่วมพร้อมไอคอนแรงค์และสถานะตรงตามภาพทั้ง 3 โหมด
  function buildParticipantsByMode(mode) {
    if (mode === 'created-by-me') {
      return [
        { name: 'มะหมาสี่ขา', badge: RANK_BADGE_ICONS[0], role: 'คุณ' },
        { name: 'มะหมาห้าขา', badge: RANK_BADGE_ICONS[1], role: 'ผู้เข้าร่วม' },
        { name: 'มะหมาหกขา', badge: RANK_BADGE_ICONS[2], role: 'ผู้เข้าร่วม' }
      ];
    }

    if (mode === 'other-joined') {
      return [
        { name: 'มะหมาสี่ขา', badge: RANK_BADGE_ICONS[0], role: 'ผู้สร้าง' },
        { name: 'มะหมาห้าขา', badge: RANK_BADGE_ICONS[1], role: 'คุณ' },
        { name: 'มะหมาหกขา', badge: RANK_BADGE_ICONS[2], role: 'ผู้เข้าร่วม' }
      ];
    }

    return [
      { name: 'มะหมาสี่ขา', badge: RANK_BADGE_ICONS[0], role: 'ผู้สร้าง' },
      { name: 'มะหมาห้าขา', badge: RANK_BADGE_ICONS[1], role: 'ผู้เข้าร่วม' },
      { name: 'มะหมาหกขา', badge: RANK_BADGE_ICONS[2], role: 'ผู้เข้าร่วม' }
    ];
  }

  async function ensurePopupLoaded() {
    if (document.getElementById('eventDetailPopup')) {
      bindModalEvents();
      return document.getElementById('eventDetailPopup');
    }

    let container = document.getElementById('eventDetailContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'eventDetailContainer';
      document.body.appendChild(container);
    }

    try {
      const response = await fetch('../html/EventDetail.html');
      if (response.ok) {
        const htmlText = await response.text();
        const parser = new DOMParser();
        const doc = parser.parseFromString(htmlText, 'text/html');
        const popupEl = doc.getElementById('eventDetailPopup');
        if (popupEl) {
          container.innerHTML = '';
          container.appendChild(popupEl);
          bindModalEvents();
          return popupEl;
        }
      }
    } catch (e) {
      // ใช้ FALLBACK_POPUP_HTML กรณีเปิดผ่าน file://
    }

    container.innerHTML = FALLBACK_POPUP_HTML;
    bindModalEvents();
    return document.getElementById('eventDetailPopup');
  }

  function renderModal() {
    const popup = document.getElementById('eventDetailPopup');
    if (!popup) return;

    const logoEl = document.getElementById('eventDetailLogo');
    const titleEl = document.getElementById('eventDetailTitle');
    const descTextEl = document.getElementById('eventDetailDescText');
    const descInputEl = document.getElementById('eventDetailDescInput');
    const descFooterEl = document.getElementById('eventDetailDescFooter');
    const charCountEl = document.getElementById('eventDetailCharCount');
    const timeEl = document.getElementById('eventDetailTime');
    const dateEl = document.getElementById('eventDetailDate');
    const locationEl = document.getElementById('eventDetailLocation');
    const capacityEl = document.getElementById('eventDetailCapacity');
    const participantHeaderEl = document.getElementById('eventDetailParticipantHeader');
    const participantListEl = document.getElementById('eventDetailParticipantList');
    const actionBtnEl = document.getElementById('eventDetailActionBtn');

    if (logoEl) logoEl.src = currentModalState.logo || '../Picture/bowl.svg';
    if (titleEl) titleEl.textContent = currentModalState.eventName || MOCK_TEMPLATE_EVENT.eventName;
    if (timeEl) timeEl.textContent = String(currentModalState.time || '18.30').replace(':', '.');
    if (dateEl) dateEl.textContent = currentModalState.date || 'Sat, 28 Sep';
    if (locationEl) locationEl.textContent = currentModalState.location || 'หอสมุดป๋วย';

    const participants = buildParticipantsByMode(currentModalState.mode);
    const participantCount = currentModalState.currentParticipants || participants.length;
    const maxCount = currentModalState.eventSize || 6;

    if (capacityEl) {
      capacityEl.textContent = `${participantCount}/${maxCount}`;
    }

    if (participantHeaderEl) {
      participantHeaderEl.textContent = `ผู้เข้าร่วม ( ${participants.length} )`;
    }

    const isCreator = currentModalState.mode === 'created-by-me';
    const desc = (currentModalState.description || '').trim();

    if (charCountEl) {
      charCountEl.textContent = `${desc.length}/200`;
    }

    if (isCreator) {
      if (descFooterEl) descFooterEl.classList.remove('hidden');
    } else {
      if (descFooterEl) descFooterEl.classList.add('hidden');
      currentModalState.isEditingDesc = false;
    }

    if (currentModalState.isEditingDesc && isCreator) {
      if (descTextEl) descTextEl.classList.add('hidden');
      if (descInputEl) {
        descInputEl.classList.remove('hidden');
        descInputEl.value = currentModalState.description || '';
        setTimeout(() => descInputEl.focus(), 10);
      }
    } else {
      if (descInputEl) descInputEl.classList.add('hidden');
      if (descTextEl) {
        descTextEl.classList.remove('hidden');
        if (desc) {
          descTextEl.textContent = desc;
          descTextEl.classList.remove('empty');
        } else {
          descTextEl.textContent = DEFAULT_DESCRIPTION_PLACEHOLDER;
          descTextEl.classList.add('empty');
        }
      }
    }

    if (participantListEl) {
      participantListEl.innerHTML = participants
        .map(
          (p) => `
          <div class="event-detail-participant-item">
            <div class="event-detail-participant-left">
              <div class="event-detail-participant-avatar"></div>
              <div class="event-detail-participant-name-wrap">
                <span class="event-detail-participant-name">${p.name}</span>
                <img src="${p.badge}" alt="Rank Badge" class="event-detail-rank-icon">
              </div>
            </div>
            <span class="event-detail-participant-role">${p.role}</span>
          </div>`
        )
        .join('');
    }

    if (actionBtnEl) {
      actionBtnEl.className = 'event-detail-action-btn';
      if (currentModalState.mode === 'created-by-me') {
        actionBtnEl.classList.add('btn-cancel-event');
        actionBtnEl.textContent = 'ยกเลิกกิจกรรม';
      } else if (currentModalState.mode === 'other-joined') {
        actionBtnEl.classList.add('btn-cancel-join');
        actionBtnEl.textContent = 'ยกเลิกการเข้าร่วม';
      } else {
        actionBtnEl.classList.add('btn-join');
        actionBtnEl.textContent = 'เข้าร่วม';
      }
    }
  }

  function extractStateFromCard(card) {
    const eventId = card?.dataset?.eventId || 'mock-teenoi';
    const studentId = getCurrentStudentId();

    const cancelBtnOnProfile = card?.querySelector('.cancel-event-btn');
    const statusTextOnProfile = card?.querySelector('.joined-status-text')?.textContent?.trim() || '';
    const joinBtnOnHome = card?.querySelector('.join-btn');

    let mode = card?.dataset?.detailMode || 'other-not-joined';

    if (!card?.dataset?.detailMode) {
      if (
        (cancelBtnOnProfile && cancelBtnOnProfile.getAttribute('data-is-owner') === 'true') ||
        statusTextOnProfile === 'สร้างโดยคุณ' ||
        card?.dataset?.creator === 'true'
      ) {
        mode = 'created-by-me';
      } else if (
        (cancelBtnOnProfile && cancelBtnOnProfile.getAttribute('data-is-owner') === 'false') ||
        statusTextOnProfile === 'เข้าร่วมแล้ว' ||
        (joinBtnOnHome && (joinBtnOnHome.dataset.joined === 'true' || joinBtnOnHome.disabled))
      ) {
        mode = 'other-joined';
      } else if (typeof allFetchedEvents !== 'undefined' && Array.isArray(allFetchedEvents)) {
        const matched = allFetchedEvents.find((ev) => String(ev.id) === String(eventId));
        if (matched) {
          if (matched.creator && String(matched.creator.studentId) === String(studentId)) {
            mode = 'created-by-me';
          } else if (
            Array.isArray(matched.participants) &&
            matched.participants.some((p) => p && String(p.studentId) === String(studentId))
          ) {
            mode = 'other-joined';
          }
        }
      }
    }

    const cardTitle = card?.querySelector('.event-title')?.textContent?.trim() || MOCK_TEMPLATE_EVENT.eventName;
    const cardLogo = card?.querySelector('.event-logo img')?.getAttribute('src') || MOCK_TEMPLATE_EVENT.logo;

    const timeSpans = card?.querySelectorAll('.event-time-date span');
    const cardTime = timeSpans?.[0]?.textContent?.trim() || MOCK_TEMPLATE_EVENT.eventTime;
    const cardDate = timeSpans?.[1]?.textContent?.trim() || MOCK_TEMPLATE_EVENT.eventDate;
    const cardLocation = card?.querySelector('.event-location')?.textContent?.trim() || MOCK_TEMPLATE_EVENT.location;

    const capText = card?.querySelector('.capacity span')?.textContent?.trim() || '3/6';
    const [curCap, maxCap] = capText.split('/').map((n) => parseInt(n, 10));

    const savedDesc = getSavedDescription(eventId, '');

    if (USE_EXACT_IMAGE_MOCK) {
      return {
        eventId: eventId,
        eventName: MOCK_TEMPLATE_EVENT.eventName,
        logo: MOCK_TEMPLATE_EVENT.logo,
        description: savedDesc,
        time: MOCK_TEMPLATE_EVENT.eventTime,
        date: MOCK_TEMPLATE_EVENT.eventDate,
        location: MOCK_TEMPLATE_EVENT.location,
        currentParticipants: 3,
        eventSize: 6,
        mode: mode,
        sourceCard: card,
        isEditingDesc: false
      };
    }

    return {
      eventId: eventId,
      eventName: cardTitle,
      logo: cardLogo,
      description: savedDesc,
      time: cardTime,
      date: cardDate,
      location: cardLocation,
      currentParticipants: !isNaN(curCap) && curCap > 0 ? curCap : 3,
      eventSize: !isNaN(maxCap) && maxCap > 0 ? maxCap : 6,
      mode: mode,
      sourceCard: card,
      isEditingDesc: false
    };
  }

  async function openEventDetailModal(cardOrMode) {
    const popup = await ensurePopupLoaded();
    if (!popup) return;

    if (typeof cardOrMode === 'string') {
      currentModalState = {
        eventId: 'mock-teenoi',
        eventName: MOCK_TEMPLATE_EVENT.eventName,
        logo: MOCK_TEMPLATE_EVENT.logo,
        description: getSavedDescription('mock-teenoi', ''),
        time: MOCK_TEMPLATE_EVENT.eventTime,
        date: MOCK_TEMPLATE_EVENT.eventDate,
        location: MOCK_TEMPLATE_EVENT.location,
        currentParticipants: 3,
        eventSize: 6,
        mode: cardOrMode,
        sourceCard: null,
        isEditingDesc: false
      };
    } else if (cardOrMode instanceof HTMLElement) {
      currentModalState = extractStateFromCard(cardOrMode);
    }

    renderModal();
    popup.classList.remove('hidden');
  }

  function closeEventDetailModal() {
    const popup = document.getElementById('eventDetailPopup');
    if (!popup) return;

    if (currentModalState.isEditingDesc) {
      commitDescriptionEdit();
    }
    popup.classList.add('hidden');
  }

  function commitDescriptionEdit() {
    const descInputEl = document.getElementById('eventDetailDescInput');
    if (descInputEl && currentModalState.mode === 'created-by-me') {
      const trimmed = descInputEl.value.slice(0, 200);
      currentModalState.description = trimmed;
      saveDescription(currentModalState.eventId, trimmed);
    }
    currentModalState.isEditingDesc = false;
    renderModal();
  }

  function syncSourceCardUI(joined) {
    const card = currentModalState.sourceCard;
    if (!card) return;

    card.dataset.detailMode = joined ? 'other-joined' : 'other-not-joined';

    const joinBtn = card.querySelector('.join-btn');
    if (joinBtn) {
      if (joined) {
        joinBtn.disabled = true;
        joinBtn.dataset.joined = 'true';
        joinBtn.setAttribute('aria-label', 'เข้าร่วมกิจกรรมแล้ว');
        joinBtn.innerHTML = '<span class="joined-check">✓</span>';
      } else {
        joinBtn.disabled = false;
        delete joinBtn.dataset.joined;
        joinBtn.setAttribute('aria-label', 'เข้าร่วมกิจกรรม');
        joinBtn.innerHTML = '<img src="../Picture/plus-mark.svg" alt="Join">';
      }
    }
  }

  let modalEventsBound = false;
  function bindModalEvents() {
    if (modalEventsBound) return;
    const popup = document.getElementById('eventDetailPopup');
    if (!popup) return;
    modalEventsBound = true;

    const closeBtn = document.getElementById('eventDetailCloseBtn');
    const editBtn = document.getElementById('eventDetailEditBtn');
    const descBox = document.getElementById('eventDetailDescBox');
    const descInput = document.getElementById('eventDetailDescInput');
    const charCount = document.getElementById('eventDetailCharCount');
    const actionBtn = document.getElementById('eventDetailActionBtn');

    popup.addEventListener('click', (e) => {
      if (e.target === popup) {
        closeEventDetailModal();
      }
    });

    if (closeBtn) {
      closeBtn.addEventListener('click', () => {
        closeEventDetailModal();
      });
    }

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && !popup.classList.contains('hidden')) {
        closeEventDetailModal();
      }
    });

    if (editBtn) {
      editBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        if (currentModalState.mode !== 'created-by-me') return;
        if (currentModalState.isEditingDesc) {
          commitDescriptionEdit();
        } else {
          currentModalState.isEditingDesc = true;
          renderModal();
        }
      });
    }

    if (descBox) {
      descBox.addEventListener('click', () => {
        if (currentModalState.mode === 'created-by-me' && !currentModalState.isEditingDesc) {
          currentModalState.isEditingDesc = true;
          renderModal();
        }
      });
    }

    if (descInput) {
      descInput.addEventListener('input', () => {
        const val = descInput.value.slice(0, 200);
        currentModalState.description = val;
        if (charCount) {
          charCount.textContent = `${val.length}/200`;
        }
        saveDescription(currentModalState.eventId, val);
      });

      descInput.addEventListener('blur', () => {
        commitDescriptionEdit();
      });
    }

    if (actionBtn) {
      actionBtn.addEventListener('click', async () => {
        const eventId = currentModalState.eventId;
        const studentId = getCurrentStudentId();

        if (currentModalState.mode === 'other-not-joined') {
          if (eventId && !String(eventId).startsWith('mock')) {
            try {
              await fetch(`${API_BASE_URL}/events/${encodeURIComponent(eventId)}/join`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId })
              });
            } catch (err) {
              // fallback local state
            }
          }
          currentModalState.mode = 'other-joined';
          syncSourceCardUI(true);
          renderModal();
        } else if (currentModalState.mode === 'other-joined') {
          if (eventId && !String(eventId).startsWith('mock')) {
            try {
              await fetch(`${API_BASE_URL}/events/${encodeURIComponent(eventId)}/leave`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId })
              });
            } catch (err) {
              // fallback local state
            }
          }
          currentModalState.mode = 'other-not-joined';
          syncSourceCardUI(false);
          renderModal();
        } else if (currentModalState.mode === 'created-by-me') {
          if (eventId && !String(eventId).startsWith('mock')) {
            try {
              await fetch(`${API_BASE_URL}/events/${encodeURIComponent(eventId)}/leave`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId })
              });
            } catch (err) {
              // fallback local state
            }
          }
          if (currentModalState.sourceCard) {
            currentModalState.sourceCard.remove();
          }
          closeEventDetailModal();
        }
      });
    }
  }

  // กรณีไม่ได้เปิดเซิร์ฟเวอร์ Backend (การ์ดว่างเปล่า) ให้สร้างการ์ดตัวอย่างเพื่อให้คลิกทดสอบได้ทันที
  function injectFallbackDemoCardsIfEmpty() {
    const homeList = document.getElementById('eventList');
    if (homeList && homeList.querySelectorAll('.event-card').length === 0) {
      homeList.innerHTML = `
        <div class="event-card" data-event-id="mock-1" data-detail-mode="other-not-joined">
          <div class="event-logo"><img src="../Picture/bowl.svg" alt="life-style"></div>
          <div class="event-info">
            <div class="event-badge">
              <span class="event-title">ตี๋น้อย คลายเหงา</span>
              <button class="event-info-btn" type="button" data-event-id="mock-1" aria-label="ดูรายละเอียดกิจกรรม">
                <img src="../Picture/Info.svg" alt="Info">
              </button>
            </div>
            <div class="event-time-date">
              <span><img src="../Picture/TimeIcon.svg" class="detail-icon" alt="Time">18.30</span>
              <span><img src="../Picture/CalendarIcon.svg" class="detail-icon" alt="Calendar">Sat, 28 Sep</span>
            </div>
            <div class="event-location">
              <img src="../Picture/LocationIcon.svg" class="detail-icon" alt="Location">หอสมุดป๋วย
            </div>
          </div>
          <div class="event-action">
            <button class="join-btn" aria-label="เข้าร่วมกิจกรรม"><img src="../Picture/plus-mark.svg" alt="Join"></button>
            <div class="capacity"><img src="../Picture/people-outline.svg" class="people-icon" alt="Participants"><span>3/6</span></div>
          </div>
        </div>
        <div class="event-card" data-event-id="mock-2" data-detail-mode="other-joined">
          <div class="event-logo"><img src="../Picture/bowl.svg" alt="life-style"></div>
          <div class="event-info">
            <div class="event-badge">
              <span class="event-title">ตี๋น้อย คลายเหงา (เข้าร่วมแล้ว)</span>
              <button class="event-info-btn" type="button" data-event-id="mock-2" aria-label="ดูรายละเอียดกิจกรรม">
                <img src="../Picture/Info.svg" alt="Info">
              </button>
            </div>
            <div class="event-time-date">
              <span><img src="../Picture/TimeIcon.svg" class="detail-icon" alt="Time">18.30</span>
              <span><img src="../Picture/CalendarIcon.svg" class="detail-icon" alt="Calendar">Sat, 28 Sep</span>
            </div>
            <div class="event-location">
              <img src="../Picture/LocationIcon.svg" class="detail-icon" alt="Location">หอสมุดป๋วย
            </div>
          </div>
          <div class="event-action">
            <button class="join-btn" disabled data-joined="true" aria-label="เข้าร่วมกิจกรรมแล้ว"><span class="joined-check">✓</span></button>
            <div class="capacity"><img src="../Picture/people-outline.svg" class="people-icon" alt="Participants"><span>3/6</span></div>
          </div>
        </div>
        <div class="event-card" data-event-id="mock-3" data-detail-mode="created-by-me" data-creator="true">
          <div class="event-logo"><img src="../Picture/bowl.svg" alt="life-style"></div>
          <div class="event-info">
            <div class="event-badge">
              <span class="event-title">ตี๋น้อย คลายเหงา (สร้างโดยคุณ)</span>
              <button class="event-info-btn" type="button" data-event-id="mock-3" aria-label="ดูรายละเอียดกิจกรรม">
                <img src="../Picture/Info.svg" alt="Info">
              </button>
            </div>
            <div class="event-time-date">
              <span><img src="../Picture/TimeIcon.svg" class="detail-icon" alt="Time">18.30</span>
              <span><img src="../Picture/CalendarIcon.svg" class="detail-icon" alt="Calendar">Sat, 28 Sep</span>
            </div>
            <div class="event-location">
              <img src="../Picture/LocationIcon.svg" class="detail-icon" alt="Location">หอสมุดป๋วย
            </div>
          </div>
          <div class="event-action">
            <button class="join-btn" disabled data-joined="true" aria-label="เข้าร่วมกิจกรรมแล้ว"><span class="joined-check">✓</span></button>
            <div class="capacity"><img src="../Picture/people-outline.svg" class="people-icon" alt="Participants"><span>3/6</span></div>
          </div>
        </div>
      `;
    }

    const activeProfileList = document.getElementById('activeEventsList');
    if (activeProfileList && activeProfileList.querySelectorAll('.event-card').length === 0) {
      activeProfileList.innerHTML = `
        <div class="event-card" data-event-id="mock-profile-1" data-detail-mode="created-by-me">
          <div class="event-logo"><img src="../Picture/bowl.svg" alt="ตี๋น้อย คลายเหงา"></div>
          <div class="event-info">
            <div class="event-badge">
              <span class="event-title">ตี๋น้อย คลายเหงา</span>
              <button class="event-info-btn" type="button" data-event-id="mock-profile-1" aria-label="ดูรายละเอียดกิจกรรม">
                <img src="../Picture/Info.svg" alt="Info">
              </button>
            </div>
            <div class="event-time-date">
              <span><img src="../Picture/TimeIcon.svg" class="detail-icon" alt="Time">18.30</span>
              <span><img src="../Picture/CalendarIcon.svg" class="detail-icon" alt="Calendar">Sat, 28 Sep</span>
            </div>
            <div class="event-location">
              <img src="../Picture/LocationIcon.svg" class="detail-icon" alt="Location">หอสมุดป๋วย
            </div>
          </div>
          <div class="event-action-right joined-action">
            <span class="joined-status-text">สร้างโดยคุณ</span>
            <button type="button" class="cancel-event-btn" data-id="mock-profile-1" data-is-owner="true" aria-label="ยกเลิกการสร้างกิจกรรม">✕</button>
          </div>
        </div>
        <div class="event-card" data-event-id="mock-profile-2" data-detail-mode="other-joined">
          <div class="event-logo"><img src="../Picture/bowl.svg" alt="ตี๋น้อย คลายเหงา"></div>
          <div class="event-info">
            <div class="event-badge">
              <span class="event-title">ตี๋น้อย คลายเหงา</span>
              <button class="event-info-btn" type="button" data-event-id="mock-profile-2" aria-label="ดูรายละเอียดกิจกรรม">
                <img src="../Picture/Info.svg" alt="Info">
              </button>
            </div>
            <div class="event-time-date">
              <span><img src="../Picture/TimeIcon.svg" class="detail-icon" alt="Time">18.30</span>
              <span><img src="../Picture/CalendarIcon.svg" class="detail-icon" alt="Calendar">Sat, 28 Sep</span>
            </div>
            <div class="event-location">
              <img src="../Picture/LocationIcon.svg" class="detail-icon" alt="Location">หอสมุดป๋วย
            </div>
          </div>
          <div class="event-action-right joined-action">
            <span class="joined-status-text">เข้าร่วมแล้ว</span>
            <button type="button" class="cancel-event-btn" data-id="mock-profile-2" data-is-owner="false" aria-label="ยกเลิกการเข้าร่วม">✕</button>
          </div>
        </div>
      `;
    }
  }

  // ดักจับการคลิกที่การ์ดหรือชื่อกิจกรรม (ยกเว้นปุ่ม + และปุ่ม ✕)
  document.addEventListener(
    'click',
    (e) => {
      const card = e.target.closest('.event-card');
      if (!card) return;

      if (e.target.closest('.join-btn, .cancel-event-btn')) {
        return;
      }

      e.stopPropagation();
      openEventDetailModal(card);
    },
    true
  );

  // บันทึกคำอธิบายอัตโนมัติเมื่อสร้างกิจกรรมในหน้า CreateEvent
  const createForm = document.querySelector('.event-form');
  if (createForm) {
    createForm.addEventListener('submit', () => {
      const descEl = document.getElementById('eventDescription');
      const nameEl = document.getElementById('eventName');
      if (descEl && descEl.value.trim()) {
        saveDescription(nameEl ? nameEl.value.trim() : 'latest', descEl.value.trim());
      }
    });
  }

  document.addEventListener('DOMContentLoaded', async () => {
    await ensurePopupLoaded();

    //setTimeout(injectFallbackDemoCardsIfEmpty, 650);

    const isStandalonePage =
      window.location.pathname.toLowerCase().endsWith('eventdetail.html');

    if (isStandalonePage) {
      const previewBar = document.getElementById('eventDetailPreviewBar');
      if (previewBar) {
        previewBar.hidden = false;
        previewBar.querySelectorAll('button[data-preview-mode]').forEach((btn) => {
          btn.addEventListener('click', () => {
            previewBar
              .querySelectorAll('button')
              .forEach((b) => b.classList.remove('active'));
            btn.classList.add('active');
            openEventDetailModal(btn.getAttribute('data-preview-mode'));
          });
        });
      }

      const params = new URLSearchParams(window.location.search);
      const initialMode = params.get('mode') || 'other-not-joined';
      openEventDetailModal(initialMode);
    }
  });

  window.openEventDetailModal = openEventDetailModal;
  window.closeEventDetailModal = closeEventDetailModal;
})();