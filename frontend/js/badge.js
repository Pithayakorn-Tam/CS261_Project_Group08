document.addEventListener("DOMContentLoaded", () => {
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

  function getBadge(count) {
    return badges.find(badge => count >= badge.min && count <= badge.max) || null;
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

  function renderBadge() {
    const count = document.querySelectorAll("#activeEventsList .event-card, #pastEventsList .event-card").length;
    const badge = getBadge(count);
    const card = createCard(badge, count);
    if (desktopCard) desktopCard.innerHTML = card;
    if (mobilePopup) mobilePopup.innerHTML = card;
    if (mobileIcon) {
      mobileIcon.src = badge?.image || "../Picture/badge/no-Badge-mobile.svg";
      mobileIcon.alt = badge ? `${badge.name} badge` : "No badge";
    }
  }

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

  const observer = new MutationObserver(renderBadge);
  ["activeEventsList", "pastEventsList"].forEach(id => {
    const container = document.getElementById(id);
    if (container) observer.observe(container, { childList: true, subtree: true });
  });
  renderBadge();
});
