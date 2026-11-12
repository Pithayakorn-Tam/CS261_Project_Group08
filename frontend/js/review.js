document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("reviewForm");
  if (!form) return;

  const groups = [...form.querySelectorAll(".rating-group")];
  const comment = document.getElementById("reviewComment");
  const commentCount = document.getElementById("commentCount");
  const error = document.getElementById("reviewError");
  const submit = form.querySelector(".review-submit");
  const params = new URLSearchParams(window.location.search);
  const eventId = params.get("eventId") || localStorage.getItem("reviewEventId") || "";
  const role = params.get("role") || localStorage.getItem("reviewRole") || "participant";
  const ratings = groups.map(() => 0);

  document.getElementById("reviewEventId").value = eventId;
  document.getElementById("reviewRole").value = role;

  function paintStars(group, value, preview = false) {
    group.querySelectorAll(".star").forEach(star => {
      const starValue = Number(star.dataset.rating);
      const selected = starValue <= value;
      star.textContent = selected ? "★" : "☆";
      star.classList.toggle("is-selected", !preview && selected);
      star.classList.toggle("is-preview", preview && selected);
      star.setAttribute("aria-checked", String(!preview && starValue === value));
    });
  }

  groups.forEach((group, groupIndex) => {
    const stars = [...group.querySelectorAll(".star")];
    stars.forEach((star, starIndex) => {
      const value = Number(star.dataset.rating);
      star.addEventListener("click", () => {
        ratings[groupIndex] = value;
        paintStars(group, value);
        error.textContent = "";
      });
      star.addEventListener("mouseenter", () => paintStars(group, value, true));
      star.addEventListener("mouseleave", () => paintStars(group, ratings[groupIndex]));
      star.addEventListener("keydown", event => {
        let nextIndex = starIndex;
        if (event.key === "ArrowRight" || event.key === "ArrowUp") nextIndex += 1;
        if (event.key === "ArrowLeft" || event.key === "ArrowDown") nextIndex -= 1;
        if (event.key === "Home") nextIndex = 0;
        if (event.key === "End") nextIndex = stars.length - 1;
        if (nextIndex !== starIndex) {
          event.preventDefault();
          stars[Math.max(0, Math.min(stars.length - 1, nextIndex))].focus();
        }
      });
    });
  });

  function updateCommentCount() {
    commentCount.textContent = `${comment.value.length}/200`;
  }
  comment.addEventListener("input", updateCommentCount);
  updateCommentCount();

  form.addEventListener("submit", event => {
    event.preventDefault();
    if (ratings.some(rating => rating === 0)) {
      error.textContent = "กรุณาให้คะแนนให้ครบทุกหัวข้อ";
      return;
    }

    const review = {
      eventId: eventId || null,
      role,
      ratings: {
        activityAccuracy: ratings[0],
        enjoyment: ratings[1],
        participationConvenience: ratings[2]
      },
      comment: comment.value.trim(),
      submittedAt: new Date().toISOString()
    };
    const savedReviews = JSON.parse(localStorage.getItem("tumeetReviews") || "[]");
    const existingIndex = savedReviews.findIndex(item => item.eventId === review.eventId && item.role === review.role);
    if (existingIndex >= 0) savedReviews[existingIndex] = review;
    else savedReviews.push(review);
    localStorage.setItem("tumeetReviews", JSON.stringify(savedReviews));

    document.dispatchEvent(new CustomEvent("review:submitted", { detail: review }));
    error.className = "review-success";
    error.textContent = "บันทึกรีวิวเรียบร้อยแล้ว";
    submit.disabled = true;
    submit.textContent = "บันทึกแล้ว";

    window.setTimeout(() => {
      window.location.href = "./Profile.html";
    }, 800);
  });
});
