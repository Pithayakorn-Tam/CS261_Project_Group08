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

  // ไอคอนแรงค์ของผู้เข้าร่วมตามลำดับในภาพต้นแบบ (กรณี Mock Standalone)
  const RANK_BADGE_ICONS = [
    '../Picture/badge/Newly-Bee.svg',
    '../Picture/badge/Baby-Bloom.svg',
    '../Picture/badge/Happy-Blossom.svg',
    '../Picture/badge/Super-Bouquet.svg'
  ];

  const BADGE_ICON_MAP = {
    'newly bee': '../Picture/badge/Newly-Bee.svg',
    'baby bloom': '../Picture/badge/Baby-Bloom.svg',
    'happy blossom': '../Picture/badge/Happy-Blossom.svg',
    'super bouquet': '../Picture/badge/Super-Bouquet.svg',
    'no badge': '../Picture/badge/no-Badge-mobile.svg'
  };

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
    creator: null,
    participants: [],
    isExpired: false,
    mode: 'other-not-joined', // 'other-not-joined' | 'other-joined' | 'created-by-me'
    sourceCard: null,
    isEditingDesc: false
  };

  function escapeHtml(text) {
    return String(text || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function getBadgeIcon(badgeName) {
    const normalized = String(badgeName || '').trim().toLowerCase();
    return BADGE_ICON_MAP[normalized] || '../Picture/badge/no-Badge-mobile.svg';
  }

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

  function findMatchedEventDto(eventId) {
    if (eventId == null) return null;
    if (typeof allFetchedEvents !== 'undefined' && Array.isArray(allFetchedEvents)) {
      const found = allFetchedEvents.find((ev) => ev && String(ev.id) === String(eventId));
      if (found) return found;
    }
    if (window.profileEventsById && window.profileEventsById[String(eventId)]) {
      return window.profileEventsById[String(eventId)];
    }
    return null;
  }

  // สร้างรายชื่อผู้เข้าร่วมพร้อมไอคอนแรงค์และสถานะ (กรณี fallback standalone preview)
  function buildParticipantsByMode(mode, desiredCount = 3) {
    let baseList;
    if (mode === 'created-by-me') {
      baseList = [
        { name: 'มะหมาสี่ขา', badge: RANK_BADGE_ICONS[0], avatar: '', role: 'คุณ' },
        { name: 'มะหมาห้าขา', badge: RANK_BADGE_ICONS[1], avatar: '', role: 'ผู้เข้าร่วม' },
        { name: 'มะหมาหกขา', badge: RANK_BADGE_ICONS[2], avatar: '', role: 'ผู้เข้าร่วม' }
      ];
    } else if (mode === 'other-joined') {
      baseList = [
        { name: 'มะหมาสี่ขา', badge: RANK_BADGE_ICONS[0], avatar: '', role: 'ผู้สร้าง' },
        { name: 'มะหมาห้าขา', badge: RANK_BADGE_ICONS[1], avatar: '', role: 'คุณ' },
        { name: 'มะหมาหกขา', badge: RANK_BADGE_ICONS[2], avatar: '', role: 'ผู้เข้าร่วม' }
      ];
    } else {
      baseList = [
        { name: 'มะหมาสี่ขา', badge: RANK_BADGE_ICONS[0], avatar: '', role: 'ผู้สร้าง' },
        { name: 'มะหมาห้าขา', badge: RANK_BADGE_ICONS[1], avatar: '', role: 'ผู้เข้าร่วม' },
        { name: 'มะหมาหกขา', badge: RANK_BADGE_ICONS[2], avatar: '', role: 'ผู้เข้าร่วม' }
      ];
    }

    const result = [...baseList];
    let idx = 4;
    while (result.length < desiredCount) {
      result.push({
        name: `ผู้เข้าร่วม ${idx}`,
        badge: RANK_BADGE_ICONS[(idx - 1) % RANK_BADGE_ICONS.length],
        avatar: '',
        role: 'ผู้เข้าร่วม'
      });
      idx += 1;
    }
    return result.slice(0, Math.max(1, desiredCount));
  }

  function buildParticipantsList(state) {
    const currentStudentId = getCurrentStudentId();

    if (state.creator) {
      const list = [];
      const seenIds = new Set();
      const creatorId = state.creator.studentId != null ? String(state.creator.studentId) : '';
      if (creatorId) seenIds.add(creatorId);

      list.push({
        studentId: creatorId,
        name: state.creator.username || state.creator.displayname_th || creatorId || 'ผู้สร้างกิจกรรม',
        badge: getBadgeIcon(state.creator.badge),
        avatar: state.creator.profileImage || '',
        role: creatorId && creatorId === currentStudentId ? 'คุณ' : 'ผู้สร้าง'
      });

      if (Array.isArray(state.participants)) {
        state.participants.forEach((p) => {
          if (!p) return;
          const pid = p.studentId != null ? String(p.studentId) : '';
          if (pid && seenIds.has(pid)) return;
          if (pid) seenIds.add(pid);

          list.push({
            studentId: pid,
            name: p.username || p.displayname_th || pid || 'ผู้เข้าร่วม',
            badge: getBadgeIcon(p.badge),
            avatar: p.profileImage || '',
            role: pid && pid === currentStudentId ? 'คุณ' : 'ผู้เข้าร่วม'
          });
        });
      }

      return list;
    }

    return buildParticipantsByMode(state.mode, state.currentParticipants || 3);
  }

  function resolveModeFromDto(dto, fallbackMode = 'other-not-joined') {
    if (!dto) return fallbackMode;
    const studentId = getCurrentStudentId();
    if (dto.creator && String(dto.creator.studentId) === studentId) {
      return 'created-by-me';
    }
    if (
      Array.isArray(dto.participants) &&
      dto.participants.some((p) => p && String(p.studentId) === studentId)
    ) {
      return 'other-joined';
    }
    return 'other-not-joined';
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
    const footerEl = document.getElementById('eventDetailFooter');
    const actionBtnEl = document.getElementById('eventDetailActionBtn');

    if (logoEl) logoEl.src = currentModalState.logo || '../Picture/bowl.svg';
    if (titleEl) titleEl.textContent = currentModalState.eventName || MOCK_TEMPLATE_EVENT.eventName;
    if (timeEl) timeEl.textContent = String(currentModalState.time || '18.30').replace(':', '.');
    if (dateEl) dateEl.textContent = currentModalState.date || 'Sat, 28 Sep';
    if (locationEl) locationEl.textContent = currentModalState.location || 'หอสมุดป๋วย';

    const participants = buildParticipantsList(currentModalState);
    const participantCount = currentModalState.creator
      ? Math.max(currentModalState.currentParticipants || 0, participants.length)
      : (currentModalState.currentParticipants || participants.length);
    const maxCount = currentModalState.eventSize || 6;

    if (capacityEl) {
      capacityEl.textContent = `${participantCount}/${maxCount}`;
    }

    if (participantHeaderEl) {
      participantHeaderEl.textContent = `ผู้เข้าร่วม ( ${participants.length} )`;
    }

    const canEditDesc = currentModalState.mode === 'created-by-me' && !currentModalState.isExpired;
    const desc = (currentModalState.description || '').trim();

    if (charCountEl) {
      charCountEl.textContent = `${desc.length}/200`;
    }

    if (canEditDesc) {
      if (descFooterEl) descFooterEl.classList.remove('hidden');
    } else {
      if (descFooterEl) descFooterEl.classList.add('hidden');
      currentModalState.isEditingDesc = false;
    }

    if (currentModalState.isEditingDesc && canEditDesc) {
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
              <div class="event-detail-participant-avatar">${
                p.avatar ? `<img src="${escapeHtml(p.avatar)}" alt="${escapeHtml(p.name)}">` : ''
              }</div>
              <div class="event-detail-participant-name-wrap">
                <span class="event-detail-participant-name">${escapeHtml(p.name)}</span>
                <img src="${escapeHtml(p.badge)}" alt="Rank Badge" class="event-detail-rank-icon">
              </div>
            </div>
            <span class="event-detail-participant-role">${escapeHtml(p.role)}</span>
          </div>`
        )
        .join('');
    }

    if (footerEl) {
      footerEl.style.display = currentModalState.isExpired ? 'none' : '';
    }

    if (actionBtnEl) {
      actionBtnEl.className = 'event-detail-action-btn';
      actionBtnEl.disabled = false;
      if (currentModalState.mode === 'created-by-me') {
        actionBtnEl.classList.add('btn-cancel-event');
        actionBtnEl.textContent = 'ยกเลิกกิจกรรม';
      } else if (currentModalState.mode === 'other-joined') {
        actionBtnEl.classList.add('btn-cancel-join');
        actionBtnEl.textContent = 'ยกเลิกการเข้าร่วม';
      } else if (participantCount >= maxCount) {
        actionBtnEl.classList.add('btn-join');
        actionBtnEl.disabled = true;
        actionBtnEl.textContent = 'กิจกรรมเต็มแล้ว';
      } else {
        actionBtnEl.classList.add('btn-join');
        actionBtnEl.textContent = 'เข้าร่วม';
      }
    }
  }

  function extractStateFromCard(card) {
    const eventId = card?.dataset?.eventId || 'mock-teenoi';
    const matched = findMatchedEventDto(eventId);
    const isExpired = Boolean(card?.classList?.contains('expired') || matched?.expired);

    const cancelBtnOnProfile = card?.querySelector('.cancel-event-btn');
    const statusTextOnProfile = card?.querySelector('.joined-status-text')?.textContent?.trim() || '';
    const joinBtnOnHome = card?.querySelector('.join-btn');

    let mode = card?.dataset?.detailMode || 'other-not-joined';

    if (!card?.dataset?.detailMode) {
      if (
        (cancelBtnOnProfile && cancelBtnOnProfile.getAttribute('data-is-owner') === 'true') ||
        statusTextOnProfile === 'สร้างโดยคุณ' ||
        card?.dataset?.creator === 'true' ||
        matched?.isOwner === true
      ) {
        mode = 'created-by-me';
      } else if (
        (cancelBtnOnProfile && cancelBtnOnProfile.getAttribute('data-is-owner') === 'false') ||
        statusTextOnProfile === 'เข้าร่วมแล้ว' ||
        (joinBtnOnHome && (joinBtnOnHome.dataset.joined === 'true' || joinBtnOnHome.disabled)) ||
        matched?.isJoined === true
      ) {
        mode = 'other-joined';
      } else if (matched) {
        mode = resolveModeFromDto(matched, mode);
      }
    }

    const cardTitle = card?.querySelector('.event-title')?.textContent?.trim() || matched?.eventName || matched?.title || MOCK_TEMPLATE_EVENT.eventName;
    const cardLogo = card?.querySelector('.event-logo img')?.getAttribute('src') || matched?.logo || MOCK_TEMPLATE_EVENT.logo;

    const timeSpans = card?.querySelectorAll('.event-time-date span');
    const cardTime = timeSpans?.[0]?.textContent?.trim() || matched?.eventTime || matched?.time || MOCK_TEMPLATE_EVENT.eventTime;
    const cardDate = timeSpans?.[1]?.textContent?.trim() || matched?.date || MOCK_TEMPLATE_EVENT.eventDate;
    const cardLocation = card?.querySelector('.event-location')?.textContent?.trim() || matched?.location || MOCK_TEMPLATE_EVENT.location;

    const capText = card?.querySelector('.capacity span')?.textContent?.trim() || '';
    const [capCur, capMax] = capText ? capText.split('/').map((n) => parseInt(n, 10)) : [NaN, NaN];
    const dataCur = parseInt(card?.dataset?.participants || '', 10);
    const dataMax = parseInt(card?.dataset?.capacity || '', 10);

    const resolvedCur = !isNaN(capCur) && capCur > 0
      ? capCur
      : (!isNaN(dataCur) && dataCur > 0 ? dataCur : (matched?.currentParticipants || 1));
    const resolvedMax = !isNaN(capMax) && capMax > 0
      ? capMax
      : (!isNaN(dataMax) && dataMax > 0 ? dataMax : (matched?.eventSize || 6));

    const initialDesc =
      (matched && matched.description != null ? matched.description : null) ??
      (card?.dataset?.description != null && card.dataset.description !== '' ? card.dataset.description : null) ??
      getSavedDescription(eventId, '');

    if (USE_EXACT_IMAGE_MOCK) {
      return {
        eventId: eventId,
        eventName: MOCK_TEMPLATE_EVENT.eventName,
        logo: MOCK_TEMPLATE_EVENT.logo,
        description: initialDesc,
        time: MOCK_TEMPLATE_EVENT.eventTime,
        date: MOCK_TEMPLATE_EVENT.eventDate,
        location: MOCK_TEMPLATE_EVENT.location,
        currentParticipants: 3,
        eventSize: 6,
        creator: null,
        participants: [],
        isExpired: isExpired,
        mode: mode,
        sourceCard: card,
        isEditingDesc: false
      };
    }

    return {
      eventId: eventId,
      eventName: cardTitle,
      logo: cardLogo,
      description: initialDesc,
      time: cardTime,
      date: cardDate,
      location: cardLocation,
      currentParticipants: resolvedCur,
      eventSize: resolvedMax,
      creator: matched?.creator || null,
      participants: Array.isArray(matched?.participants) ? matched.participants : [],
      isExpired: isExpired,
      mode: mode,
      sourceCard: card,
      isEditingDesc: false
    };
  }

  function applyEventDtoToModalState(dto) {
    if (!dto) return;
    currentModalState.eventName = dto.eventName || currentModalState.eventName;
    currentModalState.description = dto.description != null ? dto.description : '';
    currentModalState.time = dto.eventTime || currentModalState.time;
    currentModalState.location = dto.location || currentModalState.location;
    currentModalState.creator = dto.creator || null;
    currentModalState.participants = Array.isArray(dto.participants) ? dto.participants : [];
    currentModalState.currentParticipants =
      dto.currentParticipants ?? (1 + currentModalState.participants.length);
    currentModalState.eventSize = dto.eventSize ?? currentModalState.eventSize;
    currentModalState.isExpired = Boolean(
      dto.status === 'CLOSED' ||
      dto.status === 'CANCELLED' ||
      dto.lifecycleStatus === 'COMPLETED' ||
      dto.lifecycleStatus === 'CANCELLED' ||
      currentModalState.isExpired
    );
    currentModalState.mode = resolveModeFromDto(dto, currentModalState.mode);

    saveDescription(currentModalState.eventId, currentModalState.description);
    syncSourceCardData(dto);
  }

  function syncSourceCardData(dto) {
    const card = currentModalState.sourceCard;
    if (!card || !dto) return;
    if (dto.description != null) {
      card.dataset.description = dto.description;
    }
    if (dto.currentParticipants != null) {
      card.dataset.participants = String(dto.currentParticipants);
    }
    if (dto.eventSize != null) {
      card.dataset.capacity = String(dto.eventSize);
    }
    const capSpan = card.querySelector('.capacity span');
    if (capSpan && dto.currentParticipants != null && dto.eventSize != null) {
      capSpan.textContent = `${dto.currentParticipants}/${dto.eventSize}`;
    }
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
        creator: null,
        participants: [],
        isExpired: false,
        mode: cardOrMode,
        sourceCard: null,
        isEditingDesc: false
      };
    } else if (cardOrMode instanceof HTMLElement) {
      currentModalState = extractStateFromCard(cardOrMode);
    }

    renderModal();
    popup.classList.remove('hidden');

    const targetEventId = currentModalState.eventId;
    if (!USE_EXACT_IMAGE_MOCK && targetEventId && !String(targetEventId).startsWith('mock')) {
      try {
        const res = await fetch(`${API_BASE_URL}/events/${encodeURIComponent(targetEventId)}`);
        if (!res.ok) return;
        const result = await res.json();
        if (
          result &&
          result.status &&
          result.data &&
          !popup.classList.contains('hidden') &&
          String(currentModalState.eventId) === String(targetEventId)
        ) {
          applyEventDtoToModalState(result.data);
          renderModal();
        }
      } catch (err) {
        console.warn('Cannot fetch event details from backend:', err);
      }
    }
  }

  function closeEventDetailModal() {
    const popup = document.getElementById('eventDetailPopup');
    if (!popup) return;

    if (currentModalState.isEditingDesc) {
      commitDescriptionEdit();
    }
    popup.classList.add('hidden');
  }

  async function commitDescriptionEdit() {
    if (!currentModalState.isEditingDesc) return;

    const descInputEl = document.getElementById('eventDetailDescInput');
    const eventId = currentModalState.eventId;
    const isCreator = currentModalState.mode === 'created-by-me' && !currentModalState.isExpired;
    let newDesc = currentModalState.description || '';

    if (descInputEl && isCreator) {
      newDesc = descInputEl.value.trim().slice(0, 200);
      currentModalState.description = newDesc;
      saveDescription(eventId, newDesc);
      if (currentModalState.sourceCard) {
        currentModalState.sourceCard.dataset.description = newDesc;
      }
      const matched = findMatchedEventDto(eventId);
      if (matched) {
        matched.description = newDesc;
      }
    }

    currentModalState.isEditingDesc = false;
    renderModal();

    if (isCreator && eventId && !String(eventId).startsWith('mock')) {
      try {
        const res = await fetch(`${API_BASE_URL}/events/${encodeURIComponent(eventId)}/description`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            studentId: getCurrentStudentId(),
            description: newDesc
          })
        });
        if (res.ok) {
          const result = await res.json();
          if (result && result.status && result.data) {
            if (String(currentModalState.eventId) === String(eventId)) {
              currentModalState.description = result.data.description || '';
              renderModal();
            }
            saveDescription(eventId, result.data.description || '');
            syncSourceCardData(result.data);
            const matched = findMatchedEventDto(eventId);
            if (matched) {
              matched.description = result.data.description || '';
            }
          }
        }
      } catch (err) {
        console.warn('Cannot update event description on backend:', err);
      }
    }
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
        if (currentModalState.mode !== 'created-by-me' || currentModalState.isExpired) return;
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
        if (currentModalState.mode === 'created-by-me' && !currentModalState.isExpired && !currentModalState.isEditingDesc) {
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
        if (currentModalState.isExpired) return;

        const eventId = currentModalState.eventId;
        const studentId = getCurrentStudentId();
        const isRealEvent = Boolean(eventId && !String(eventId).startsWith('mock'));

        if (currentModalState.mode === 'other-not-joined') {
          if (isRealEvent) {
            actionBtn.disabled = true;
            try {
              const res = await fetch(`${API_BASE_URL}/events/${encodeURIComponent(eventId)}/join`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId })
              });
              const result = await res.json();
              if (!res.ok || !result.status) {
                alert((result && result.message) || 'ไม่สามารถเข้าร่วมกิจกรรมได้');
                actionBtn.disabled = false;
                return;
              }
              if (result.data) {
                applyEventDtoToModalState(result.data);
              }
            } catch (err) {
              alert('เกิดข้อผิดพลาดในการเชื่อมต่อเพื่อเข้าร่วมกิจกรรม');
              actionBtn.disabled = false;
              return;
            }
          } else {
            currentModalState.mode = 'other-joined';
          }
          syncSourceCardUI(true);
          renderModal();
          if (typeof window.refreshNotifications === 'function') window.refreshNotifications();
          if (typeof window.refreshHomeEvents === 'function') window.refreshHomeEvents();
          if (typeof window.refreshProfileEvents === 'function') window.refreshProfileEvents();
        } else if (currentModalState.mode === 'other-joined') {
          if (isRealEvent) {
            actionBtn.disabled = true;
            try {
              const res = await fetch(`${API_BASE_URL}/events/${encodeURIComponent(eventId)}/leave`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId })
              });
              const result = await res.json();
              if (!res.ok || !result.status) {
                alert((result && result.message) || 'ไม่สามารถยกเลิกการเข้าร่วมกิจกรรมได้');
                actionBtn.disabled = false;
                return;
              }
              if (result.data) {
                applyEventDtoToModalState(result.data);
              }
            } catch (err) {
              alert('เกิดข้อผิดพลาดในการเชื่อมต่อเพื่อยกเลิกการเข้าร่วมกิจกรรม');
              actionBtn.disabled = false;
              return;
            }
          } else {
            currentModalState.mode = 'other-not-joined';
          }
          syncSourceCardUI(false);
          renderModal();
          if (typeof window.refreshNotifications === 'function') window.refreshNotifications();
          if (typeof window.refreshHomeEvents === 'function') window.refreshHomeEvents();
          if (typeof window.refreshProfileEvents === 'function') window.refreshProfileEvents();
        } else if (currentModalState.mode === 'created-by-me') {
          if (isRealEvent) {
            actionBtn.disabled = true;
            try {
              const res = await fetch(`${API_BASE_URL}/events/${encodeURIComponent(eventId)}/leave`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId })
              });
              const result = await res.json();
              if (!res.ok || !result.status) {
                alert((result && result.message) || 'ไม่สามารถยกเลิกกิจกรรมได้');
                actionBtn.disabled = false;
                return;
              }
            } catch (err) {
              alert('เกิดข้อผิดพลาดในการเชื่อมต่อเพื่อยกเลิกกิจกรรม');
              actionBtn.disabled = false;
              return;
            }
          }
          if (currentModalState.sourceCard) {
            currentModalState.sourceCard.remove();
          }
          closeEventDetailModal();
          if (typeof window.refreshNotifications === 'function') window.refreshNotifications();
          if (typeof window.refreshHomeEvents === 'function') window.refreshHomeEvents();
          if (typeof window.refreshProfileEvents === 'function') window.refreshProfileEvents();
        }
      });
    }
  }

  // ดักจับการคลิกที่การ์ดหรือชื่อกิจกรรม (ยกเว้นปุ่ม +, ปุ่ม ✕ และปุ่มประเมิน)
  document.addEventListener(
    'click',
    (e) => {
      const card = e.target.closest('.event-card');
      if (!card) return;

      if (e.target.closest('.join-btn, .cancel-event-btn, .review-action')) {
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
