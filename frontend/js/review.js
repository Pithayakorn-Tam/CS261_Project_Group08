document.addEventListener("DOMContentLoaded", () => {
  const API_BASE_URL = (window.location.protocol === "file:" || window.location.port === "5500" || window.location.port === "5501")
    ? "http://localhost:8080/api"
    : "/api";

  const form = document.getElementById("reviewForm");
  if (!form) return;

  const groups = [...form.querySelectorAll(".rating-group")];
  const comment = document.getElementById("reviewComment");
  const commentCount = document.getElementById("commentCount");
  const error = document.getElementById("reviewError");
  const submit = form.querySelector(".review-submit");
  const successModal = document.getElementById("reviewSuccessModal");
  const successTitle = document.getElementById("reviewSuccessTitle");
  successTitle.textContent = "\u0e1b\u0e23\u0e30\u0e40\u0e21\u0e34\u0e19\u0e01\u0e34\u0e08\u0e01\u0e23\u0e23\u0e21\u0e40\u0e2a\u0e23\u0e47\u0e08\u0e2a\u0e34\u0e49\u0e19";
  const params = new URLSearchParams(window.location.search);
  const eventId = params.get("eventId") || localStorage.getItem("reviewEventId") || "";
  const role = params.get("role") || localStorage.getItem("reviewRole") || "participant";
  const ratings = groups.map(() => 0);

  document.getElementById("reviewEventId").value = eventId;
  document.getElementById("reviewRole").value = role;

  function getCurrentStudentId() {
    try {
      const storedUserData = JSON.parse(localStorage.getItem("loginData") || "{}");
      if (storedUserData.studentId) return String(storedUserData.studentId);
      if (localStorage.getItem("studentId")) return String(localStorage.getItem("studentId"));
    } catch (e) {
      console.warn("Cannot parse loginData:", e);
    }
    return "6800000000";
  }

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
        if (star.disabled) return;
        ratings[groupIndex] = value;
        paintStars(group, value);
        error.textContent = "";
      });
      star.addEventListener("mouseenter", () => {
        if (!star.disabled) paintStars(group, value, true);
      });
      star.addEventListener("mouseleave", () => {
        if (!star.disabled) paintStars(group, ratings[groupIndex]);
      });
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

  function disableReviewForm(message, statusClassName, submitText) {
    groups.forEach(group => {
      group.querySelectorAll(".star").forEach(star => {
        star.disabled = true;
      });
    });
    comment.disabled = true;
    error.className = statusClassName;
    error.textContent = message;
    submit.disabled = true;
    if (submitText) {
      submit.textContent = submitText;
    }
  }

  function closeSuccessModalAndGoProfile() {
    if (!successModal || successModal.hidden) return;
    successModal.hidden = true;
    window.location.href = "./Profile.html";
  }

  if (successModal) {
    successModal.addEventListener("click", event => {
      if (event.target === successModal) {
        closeSuccessModalAndGoProfile();
      }
    });
  }

  document.addEventListener("keydown", event => {
    if (event.key === "Escape" && successModal && !successModal.hidden) {
      closeSuccessModalAndGoProfile();
    }
  });

  async function loadReviewStatus() {
    if (!eventId) return;
    try {
      const studentId = getCurrentStudentId();
      const res = await fetch(
        `${API_BASE_URL}/events/${encodeURIComponent(eventId)}/ratings/status?studentId=${encodeURIComponent(studentId)}`
      );
      if (!res.ok) return;
      const result = await res.json();
      const data = result && result.data;
      if (data && data.isCreator) {
        disableReviewForm(
          "ผู้สร้างกิจกรรมไม่สามารถประเมินกิจกรรมของตัวเองได้",
          "review-error",
          "ไม่สามารถประเมินได้"
        );
        return;
      }
      if (data && data.reviewed) {
        const prefilled = [data.q1Score, data.q2Score, data.q3Score, data.q4Score];
        groups.forEach((group, idx) => {
          const scoreVal = Number(prefilled[idx]) || Number(data.score) || 0;
          ratings[idx] = scoreVal;
          paintStars(group, scoreVal);
        });
        comment.value = data.comment || "";
        updateCommentCount();
        disableReviewForm(
          "คุณได้ประเมินกิจกรรมนี้ไปแล้ว",
          "review-success",
          "ประเมินแล้ว"
        );
      }
    } catch (err) {
      console.warn("ไม่สามารถตรวจสอบสถานะการประเมินได้:", err);
    }
  }
  loadReviewStatus();

  form.addEventListener("submit", async event => {
    event.preventDefault();
    if (!eventId) {
      error.className = "review-error";
      error.textContent = "ไม่พบรหัสกิจกรรมสำหรับการประเมิน";
      return;
    }
    if (ratings.some(rating => rating === 0)) {
      error.className = "review-error";
      error.textContent = "กรุณาให้คะแนนให้ครบทุกหัวข้อ";
      return;
    }

    const studentId = getCurrentStudentId();
    const trimmedComment = comment.value.trim();

    submit.disabled = true;
    error.textContent = "";

    try {
      const res = await fetch(`${API_BASE_URL}/events/${encodeURIComponent(eventId)}/ratings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          raterStudentId: studentId,
          role,
          q1Score: ratings[0],
          q2Score: ratings[1],
          q3Score: ratings[2],
          q4Score: ratings[3],
          comment: trimmedComment
        })
      });

      const result = await res.json().catch(() => ({}));
      if (!res.ok || !result.status) {
        error.className = "review-error";
        error.textContent = result.message || "ไม่สามารถบันทึกผลการประเมินได้";
        submit.disabled = false;
        return;
      }

      const review = {
        eventId: eventId || null,
        role,
        ratings: {
          activityAccuracy: ratings[0],
          enjoyment: ratings[1],
          participationConvenience: ratings[2],
          uiAppearance: ratings[3]
        },
        comment: trimmedComment,
        submittedAt: new Date().toISOString()
      };
      const savedReviews = JSON.parse(localStorage.getItem("tumeetReviews") || "[]");
      const existingIndex = savedReviews.findIndex(item => String(item.eventId) === String(review.eventId) && item.role === review.role);
      if (existingIndex >= 0) savedReviews[existingIndex] = review;
      else savedReviews.push(review);
      localStorage.setItem("tumeetReviews", JSON.stringify(savedReviews));

      document.dispatchEvent(new CustomEvent("review:submitted", { detail: review }));
      error.className = "review-success";
      error.textContent = "บันทึกรีวิวเรียบร้อยแล้ว";
      submit.disabled = true;
      submit.textContent = "บันทึกแล้ว";

      successModal.hidden = false;
    } catch (err) {
      console.error("เกิดข้อผิดพลาดในการส่งผลการประเมิน:", err);
      error.className = "review-error";
      error.textContent = "ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้ กรุณาลองใหม่อีกครั้ง";
      submit.disabled = false;
    }
  });
});
