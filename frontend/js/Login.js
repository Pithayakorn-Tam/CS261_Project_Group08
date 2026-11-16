// จัดการการส่งฟอร์มและเชื่อมต่อ API
const loginForm = document.getElementById('loginForm');

loginForm.addEventListener('submit', async function(event) {
    // 1. ป้องกันไม่ให้หน้าเว็บรีเฟรช
    event.preventDefault();

    // 2. ดึงค่าจากฟอร์ม (เปลี่ยนจาก email เป็น IdNum)
    const studentId = document.getElementById('studentId').value.trim();
    const idNum = document.getElementById('IdNum').value.trim();
    const submitBtn = document.getElementById('submitBtn');
    const errorBox = document.getElementById('errorBox');

    // 3. เตรียมสถานะระหว่างรอข้อมูลจาก Backend
    errorBox.classList.add('hidden');
    submitBtn.disabled = true;
    submitBtn.textContent = 'CHECKING...';

    try {
        // 4. ส่งข้อมูลไปยัง Backend 
        const response = await fetch('http://localhost:8080/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                studentId: studentId,
                idNum: idNum // ตรวจสอบชื่อ key ให้ตรงกับที่ Controller / Backend ฝั่งคุณรอรับ
            })
        });

        const data = await response.json();

        // 5. ตรวจสอบสถานะการตอบกลับ
        if (!response.ok) {
            throw new Error(data.message || 'รหัสนักศึกษาหรือเลขบัตรประชาชนไม่ถูกต้อง');
        }

        // กรณีสำเร็จ: บันทึกข้อมูลผู้ใช้จาก Backend ลง localStorage เพื่อให้หน้า Profile และ CreateEvent นำไปใช้ต่อได้ทันที
        const resolvedStudentId = data.studentId || studentId;
        localStorage.setItem('loginData', JSON.stringify({
            studentId: resolvedStudentId,
            email: data.email || `${resolvedStudentId}@dome.tu.ac.th`,
            username: data.displayname_th || data.username || resolvedStudentId,
            displayname_th: data.displayname_th || '',
            displayname_en: data.displayname_en || '',
            faculty: data.faculty || '',
            department: data.department || ''
        }));
        localStorage.setItem('studentId', resolvedStudentId);
        
        alert('เข้าสู่ระบบสำเร็จ!');
        window.location.href = '../html/Homepage.html';

    } catch (error) {
        // 6. แสดงข้อความแจ้งเตือนเมื่อเกิดข้อผิดพลาด
        errorBox.textContent = error.message || "Failed to login, please try again.";
        errorBox.classList.remove('hidden');
    } finally {
        // 7. คืนค่าปุ่มให้ใช้งานได้ปกติ
        submitBtn.disabled = false;
        submitBtn.textContent = 'LOGIN';
    }
});

// ดึง element ของช่องกรอกข้อมูล
const studentIdInput = document.getElementById('studentId');
const idNumInput = document.getElementById('IdNum');

// เปลี่ยนข้อความเตือนเมื่อไม่ได้กรอก Student ID
studentIdInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณากรอกรหัสนักศึกษา');
};
studentIdInput.oninput = function(event) {
    event.target.setCustomValidity(''); // ล้างข้อความเตือนเมื่อเริ่มพิมพ์
};

// เปลี่ยนข้อความเตือนเมื่อไม่ได้กรอก ID Number
idNumInput.oninvalid = function(event) {
    event.target.setCustomValidity('กรุณากรอกเลขบัตรประชาชน');
};
idNumInput.oninput = function(event) {
    event.target.setCustomValidity(''); // ล้างข้อความเตือนเมื่อเริ่มพิมพ์
};