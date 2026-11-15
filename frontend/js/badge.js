document.addEventListener("DOMContentLoaded", () => {
  const API_BASE_URL = (window.location.protocol === "file:" || window.location.port === "5500" || window.location.port === "5501")
    ? "http://localhost:8080/api"
    : "/api";

  const badges = [
    { name: "Newly Bee", image: "../Picture/badge/Newly-Bee.svg", min: 1, max: 4 },
    { name: "Baby Bloom", image: "../Picture/badge/Baby-Bloom.svg", min: 5, max: 9 },
    { name: "Happy Blossom", image: "../Picture/badge/Happy-Blossom.svg", min: 10, max: 19 },
    { name: "Super Bouquet", image: "../Picture/badge/Super-Bouquet.svg", min: 20, max: Infinity }
  ];
  const trigger = document.getElementById("usernameBadgeTrigger");
  const mobilePopup = document.getElementById("mobileBadgePopup");
  const desktopCard = document.getElementById("desktopBadgeCard");
  const mobileIcon = trigger?.querySelector(".username-side-icon");

  function getCurrentStudentId() {
    try {
      const storedUserData = JSON.parse(localStorage.getItem("loginData") || "{}");
      if (storedUserData.studentId) return storedUserData.studentId;
      if (localStorage.getItem("studentId")) return localStorage.getItem("studentId");
    } catch (e) {
      console.warn("Cannot parse loginData:", e);
    }
    return "6800000000";
  }

  function getBadge(count) {
    return badges.find(badge => count >= badge.min && count <= badge.max) || null;
  }

  function getBadgeByNameOrCount(badgeName, count) {
    if (badgeName && badgeName !== "No Badge") {
      const matched = badges.find(b => b.name.toLowerCase() === String(badgeName).trim().toLowerCase());
      if (matched) return matched;
    }
    if (badgeName === "No Badge") return null;
    return getBadge(count);
  }

  function createCard(badge, count) {
    if (!badge) return '<div class="badge-card badge-card-empty"><span>no badge</span></div>';
    const range = badge.max === Infinity ? `${badge.min} ครั้งขึ้นไป` : `${badge.min} - ${badge.max} ครั้ง`;
    return `<article class="badge-card">
      <img class="badge-card-image" src="${badge.image}" alt="${badge.name}">
      <div class="badge-card-content">
        <h3 class="badge-card-name">${badge.name}</h3>
        <p class="badge-card-description">สำหรับผู้เข้าร่วมกิจกรรม ${range}</p>
        <p class="badge-card-progress">คุณเคยเข้าร่วมกิจกรรม ${count} ครั้ง</p>
      </div>
    </article>`;
  }

  function applyBadgeRender(badge, count) {
    const card = createCard(badge, count);
    if (desktopCard) desktopCard.innerHTML = card;
    if (mobilePopup) mobilePopup.innerHTML = card;
    if (mobileIcon) {
      mobileIcon.src = badge?.image || "../Picture/badge/no-Badge-mobile.svg";
      mobileIcon.alt = badge ? `${badge.name} badge` : "No badge";
    }
  }

  function renderBadgeFromUser(user) {
    const created = Number(user?.completedCreatedCount) || 0;
    const joined = Number(user?.completedJoinedCount) || 0;
    const totalCount = created + joined;
    const badge = getBadgeByNameOrCount(user?.badge, totalCount);
    applyBadgeRender(badge, totalCount);
  }

  function renderFallbackBadgeFromDom() {
    const count = document.querySelectorAll("#pastEventsList .event-card").length;
    const badge = getBadge(count);
    applyBadgeRender(badge, count);
  }

  async function fetchAndRenderBadge() {
    const studentId = getCurrentStudentId();
    try {
      const res = await fetch(`${API_BASE_URL}/user/${encodeURIComponent(studentId)}`);
      if (!res.ok) {
        renderFallbackBadgeFromDom();
        return;
      }
      const result = await res.json();
      if (result && result.data) {
        renderBadgeFromUser(result.data);
      } else {
        renderFallbackBadgeFromDom();
      }
    } catch (error) {
      console.warn("ไม่สามารถโหลดข้อมูล Badge จาก Backend:", error);
      renderFallbackBadgeFromDom();
    }
  }

  window.renderUserBadgeFromData = renderBadgeFromUser;
  window.refreshUserBadge = fetchAndRenderBadge;

  trigger?.addEventListener("click", () => {
    const isOpen = trigger.getAttribute("aria-expanded") === "true";
    trigger.setAttribute("aria-expanded", String(!isOpen));
    if (mobilePopup) mobilePopup.hidden = isOpen;
  });

  document.addEventListener("click", event => {
    if (!event.target.closest(".username-row")) {
      trigger?.setAttribute("aria-expanded", "false");
      if (mobilePopup) mobilePopup.hidden = true;
    }
  });

  fetchAndRenderBadge();
});
