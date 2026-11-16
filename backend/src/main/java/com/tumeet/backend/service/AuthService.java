package com.tumeet.backend.service;

import com.tumeet.backend.dto.LoginRequest;
import com.tumeet.backend.dto.LoginResponse;
import com.tumeet.backend.dto.TuApiResponse;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    // External TU API v1 verify endpoint configured in application.properties
    @Value("${tu.api.url:https://restapi.tu.ac.th/api/v1/auth/Ad/verify}")
    private String tuApiUrl;

    // TU Application-Key configured in application.properties / .env (tu.api.key / TU_API_KEY)
    @Value("${tu.api.key:${TU_API_KEY:}}")
    private String tuApiKey;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = createSslReadyRestTemplate();
    }

    /**
     * Creates a RestTemplate that accepts restapi.tu.ac.th's SSL certificate chain
     * (prevents Java "PKIX path building failed: SunCertPathBuilderException").
     */
    private static RestTemplate createSslReadyRestTemplate() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                    if (connection instanceof HttpsURLConnection httpsConnection) {
                        httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                        httpsConnection.setHostnameVerifier((hostname, session) -> true);
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };

            return new RestTemplate(requestFactory);
        } catch (Exception e) {
            return new RestTemplate();
        }
    }

    /**
     * Authenticates a student using studentId (UserName) and citizen ID / password (PassWord)
     * via TU REST API v1: POST https://restapi.tu.ac.th/api/v1/auth/Ad/verify
     */
    public LoginResponse authenticate(LoginRequest request) {
        if (request == null || isBlank(request.getStudentId()) || isBlank(request.getIdNum())) {
            return LoginResponse.error("กรุณากรอกรหัสนักศึกษาและเลขบัตรประชาชน");
        }

        String studentId = request.getStudentId().trim();
        String idNum = request.getIdNum().trim();

        // =========================================================================
        // STEP 1: Handle mock accounts (6800000000 - 6800000009)
        // Never sent to TU API, password not stored in DB, strictly compared in-memory.
        // =========================================================================
        if (studentId.matches("^680000000[0-9]$")) {
            if ("1111111111111".equals(idNum)) {
                Optional<User> mockUserOpt = userRepository.findByStudentId(studentId);
                if (mockUserOpt.isPresent()) {
                    return LoginResponse.success(mockUserOpt.get());
                } else {
                    return LoginResponse.error("ไม่พบข้อมูลผู้ใช้จำลองในระบบ");
                }
            } else {
                return LoginResponse.error("รหัสนักศึกษาหรือรหัสผ่านไม่ถูกต้อง");
            }
        }

        // =========================================================================
        // STEP 2: Verify credentials with external TU REST API v1 (POST)
        // =========================================================================
        if (isBlank(tuApiKey)) {
            return LoginResponse.error("ยังไม่ได้ตั้งค่า TU Application-Key ในระบบ");
        }

        try {
            // Prepare HTTP Headers with the required TU Application-Key
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Application-Key", tuApiKey.trim());

            // Prepare JSON body: {"UserName": "<studentId>", "PassWord": "<idNum>"}
            Map<String, String> requestBody = new LinkedHashMap<>();
            requestBody.put("UserName", studentId);
            requestBody.put("PassWord", idNum);

            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);

            // Send POST request to TU REST API v1 (/api/v1/auth/Ad/verify)
            ResponseEntity<TuApiResponse> apiResponse = restTemplate.exchange(
                    tuApiUrl,
                    HttpMethod.POST,
                    httpEntity,
                    TuApiResponse.class
            );

            TuApiResponse tuBody = apiResponse.getBody();

            // Validate response status returned from TU API
            if (tuBody == null || !tuBody.isStatus()) {
                String apiMsg = (tuBody != null && !isBlank(tuBody.getMessage()))
                        ? tuBody.getMessage()
                        : "รหัสนักศึกษาหรือเลขบัตรประชาชนไม่ถูกต้อง";
                return LoginResponse.error(apiMsg);
            }

            // =========================================================================
            // STEP 3: Save or update verified student profile in local database
            //         (status and message from TU API are NOT stored in User entity)
            // =========================================================================
            String verifiedStudentId = !isBlank(tuBody.getUsername()) ? tuBody.getUsername().trim() : studentId;
            String verifiedEmail = !isBlank(tuBody.getEmail())
                    ? tuBody.getEmail().trim()
                    : verifiedStudentId + "@dome.tu.ac.th";

            User user = userRepository.findByStudentId(verifiedStudentId).orElseGet(User::new);
            user.setStudentId(verifiedStudentId);
            user.setEmail(verifiedEmail);
            user.setType(tuBody.getType());
            user.setTu_status(tuBody.getTu_status());
            user.setStatusid(tuBody.getStatusid());
            user.setStatusname(tuBody.getTu_status());
            user.setDisplayname_th(tuBody.getDisplayname_th());
            user.setDisplayname_en(tuBody.getDisplayname_en());
            user.setFaculty(tuBody.getFaculty());
            user.setDepartment(tuBody.getDepartment());

            User savedUser = userRepository.save(user);
            return LoginResponse.success(savedUser);

        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return LoginResponse.error("TU API ปฏิเสธ Application-Key (Unauthorized/Forbidden)");
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return LoginResponse.error("รหัสนักศึกษาหรือเลขบัตรประชาชนไม่ถูกต้อง");
            }
            return LoginResponse.error("ไม่สามารถตรวจสอบข้อมูลกับ TU API ได้: " + e.getStatusCode());
        } catch (Exception e) {
            return LoginResponse.error("เกิดข้อผิดพลาดในการเชื่อมต่อ TU API: " + e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
