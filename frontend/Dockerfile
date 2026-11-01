FROM nginx:alpine

# ลบไฟล์เริ่มต้นของ Nginx
RUN rm -rf /usr/share/nginx/html/*

# คัดลอกไฟล์ตั้งค่า Nginx (รองรับ Static Files + Reverse Proxy /api/)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# คัดลอกไฟล์และโฟลเดอร์ของ Frontend เข้าสู่โฟลเดอร์เสิร์ฟของ Nginx
COPY index.html /usr/share/nginx/html/index.html
COPY html /usr/share/nginx/html/html
COPY css /usr/share/nginx/html/css
COPY js /usr/share/nginx/html/js
COPY Font /usr/share/nginx/html/Font
COPY Picture /usr/share/nginx/html/Picture

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
