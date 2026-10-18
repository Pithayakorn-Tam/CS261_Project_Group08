
let joinPopupInitialized = false;

function initJoinPopup() {
    const popup = document.getElementById('joinPopup');

    // ป้องกันการผูก Event ซ้ำ
    if (!popup || joinPopupInitialized) return;
    joinPopupInitialized = true;

    const title = document.getElementById('joinPopupTitle');
    const message = document.getElementById('joinPopupMessage');
    const icon = document.getElementById('joinPopupIcon');
    const closeBtn = document.getElementById('joinPopupClose');

    let lastButton = null;

    // แสดง Popup
    function showPopup(success, heading, detail) {
        title.textContent = heading;
        message.textContent = detail;

        icon.className = success
            ? 'join-popup-icon success'
            : 'join-popup-icon error';

        popup.classList.remove('hidden');
        closeBtn.focus();
    }

    // ปิด Popup
    function closePopup() {
        popup.classList.add('hidden');

        if (lastButton && lastButton.isConnected) {
            lastButton.focus();
        }
    }

    // รับการคลิกปุ่ม Join บนหน้า Home
    document.addEventListener('click', function (event) {
        const button = event.target.closest('.join-btn');

        if (!button || button.disabled) return;

        const card = button.closest('.event-card');
        if (!card) return;

        lastButton = button;

        // อ่านเงื่อนไขจากข้อมูลของการ์ด
        const capacity = Number(card.dataset.capacity);
        const participants = Number(card.dataset.participants);
        const isOpen = card.dataset.open !== 'false';
        const isCancelled = card.dataset.cancelled === 'true';
        const alreadyJoined = button.dataset.joined === 'true';

        if (isCancelled) {
            showPopup(
                false,
                'ไม่สามารถเข้าร่วมกิจกรรมได้',
                'เนื่องจากกิจกรรมนี้ถูกยกเลิกแล้ว'
            );
            return;
        }

        if (!isOpen) {
            showPopup(
                false,
                'ไม่สามารถเข้าร่วมกิจกรรมได้',
                'เนื่องจากสิ้นสุดเวลารับสมัครแล้ว'
            );
            return;
        }

        if (alreadyJoined) {
            showPopup(
                false,
                'ไม่สามารถเข้าร่วมกิจกรรมได้',
                'เนื่องจากคุณเข้าร่วมกิจกรรมนี้แล้ว'
            );
            return;
        }

        if (
            !Number.isInteger(capacity) ||
            !Number.isInteger(participants) ||
            capacity <= 0 ||
            participants < 0
        ) {
            showPopup(
                false,
                'เกิดข้อผิดพลาด',
                'ไม่สามารถตรวจสอบข้อมูลกิจกรรมได้'
            );
            return;
        }

        if (participants >= capacity) {
            showPopup(
                false,
                'ไม่สามารถเข้าร่วมกิจกรรมได้',
                'เนื่องจากกิจกรรมนี้มีผู้เข้าร่วมครบจำนวนแล้ว'
            );
            return;
        }

        /*
         * จำลองการเข้าร่วมสำเร็จสำหรับทดสอบหน้าเว็บ
         * ระบบจริงต้องส่งคำขอไป Backend ก่อน
         */

        const newParticipants = participants + 1;
        card.dataset.participants = String(newParticipants);
        button.dataset.joined = 'true';
        button.disabled = true;
        button.textContent = '✓';

        const capacityElement = card.querySelector('.capacity');

        if (capacityElement) {
            capacityElement.textContent =
                `${newParticipants}/${capacity}`;
        }

        showPopup(
            true,
            'เข้าร่วมกิจกรรมสำเร็จ',
            'คุณเข้าร่วมกิจกรรมนี้เรียบร้อยแล้ว'
        );
    });

    // ปุ่มตกลง
    closeBtn.addEventListener('click', closePopup);

    // คลิกด้านนอกกล่องเพื่อปิด
    popup.addEventListener('click', function (event) {
        if (event.target === popup) {
            closePopup();
        }
    });

    // กด Escape เพื่อปิด
    document.addEventListener('keydown', function (event) {
        if (
            event.key === 'Escape' &&
            !popup.classList.contains('hidden')
        ) {
            closePopup();
        }
    });
}
