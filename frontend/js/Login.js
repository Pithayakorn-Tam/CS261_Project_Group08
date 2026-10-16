// จัดการการส่งฟอร์มและเชื่อมต่อ API
const loginForm = document.getElementById('loginForm');

loginForm.addEventListener('submit', async function(event) {
    // 1. ป้องกันไม่ให้หน้าเว็บรีเฟรช
    event.preventDefault();

    // 2. ดึงค่าจากฟอร์ม
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const submitBtn = document.getElementById('submitBtn');
    const errorBox = document.getElementById('errorBox');

    // 3. เตรียมสถานะระหว่างรอข้อมูลจาก Backend
    errorBox.classList.add('hidden');
    submitBtn.disabled = true;
    submitBtn.textContent = 'CHECKING...';

    try {
        // 4. ส่งข้อมูลไปยัง Backend 
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();

        // 5. ตรวจสอบสถานะการตอบกลับ
        if (!response.ok) {
            throw new Error(data.message || 'รหัสนักศึกษาหรือรหัสผ่านไม่ถูกต้อง');
        }

        // กรณีสำเร็จ
        alert('เข้าสู่ระบบสำเร็จ!');
        // window.location.href = '/dashboard.html';

    } catch (error) {
        // 6. แสดงข้อความแจ้งเตือนเมื่อเกิดข้อผิดพลาด
        errorBox.textContent = "failed to login, please try again.";
        errorBox.classList.remove('hidden');
    } finally {
        // 7. คืนค่าปุ่มให้ใช้งานได้ปกติ
        submitBtn.disabled = false;
        submitBtn.textContent = 'LOGIN';
    }
    
});
// ดึง element ของช่องกรอกข้อมูล
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');

// เปลี่ยนข้อความเตือนเมื่อไม่ได้กรอก Username
usernameInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณากรอกรหัสนักศึกษา');
};
usernameInput.oninput = function(event) {
    event.target.setCustomValidity(''); // ล้างข้อความเตือนเมื่อผู้ใช้เริ่มพิมพ์
};

// เปลี่ยนข้อความเตือนเมื่อไม่ได้กรอก Password
passwordInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณากรอกรหัสผ่าน');
};
passwordInput.oninput = function(event) {
    event.target.setCustomValidity(''); // ล้างข้อความเตือนเมื่อผู้ใช้เริ่มพิมพ์
};