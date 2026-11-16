// กำหนด Base URL ของ API อัตโนมัติ:
// - หากรันผ่าน Nginx (พอร์ต 80 หรือ Reverse Proxy) จะเรียกผ่าน '/api'
// - หากรันผ่าน Live Server (พอร์ต 5500/5501) หรือเปิดไฟล์โดยตรง (file://) จะเรียกไปที่ 'http://localhost:8080/api'
window.API_BASE_URL =
    window.location.protocol === 'file:' ||
    window.location.port === '5500' ||
    window.location.port === '5501'
        ? 'http://localhost:8080/api'
        : '/api';
