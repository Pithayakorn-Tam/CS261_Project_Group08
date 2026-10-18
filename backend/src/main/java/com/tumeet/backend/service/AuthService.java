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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    // External TU API base URL configured in application.properties
    @Value("${tu.api.url:https://restapi.tu.ac.th/api/v2/profile/std/info/?id=}")
    private String tuApiUrl;

    // TODO: Provide the actual TU Application-Key token in application.properties (tu.api.key)
    // once the token is generated from the TU API portal.
    @Value("${tu.api.key:}")
    private String tuApiKey;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Authenticates a student using studentId and email.
     * Step 1: Checks the local database first (includes seeded mock users 6800000001-6800000010
     *         and previously verified students).
     * Step 2: If not found in the local database, calls the external TU REST API to verify
     *         the student profile and saves the record to the database for future logins.
     */
    public LoginResponse authenticate(LoginRequest request) {
        if (request == null || isBlank(request.getStudentId()) || isBlank(request.getEmail())) {
            return LoginResponse.error("studentId and email are required");
        }

        String studentId = request.getStudentId().trim();
        String email = request.getEmail().trim();

        // =========================================================================
        // STEP 1: Check local database first (for returning users & mock accounts)
        // =========================================================================
        Optional<User> existingUserOpt = userRepository.findByStudentId(studentId);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Verify that the provided email matches the stored email
            if (existingUser.getEmail() != null && existingUser.getEmail().equalsIgnoreCase(email)) {
                return LoginResponse.success();
            } else {
                return LoginResponse.error("Email does not match the student ID in database");
            }
        }

        // =========================================================================
        // STEP 2: Verify with external TU REST API (for first-time login)
        // =========================================================================
        // TODO: Before calling the external TU API in production:
        // 1. Register the project URL in the TU API portal to obtain an Application-Key.
        // 2. Put the Application-Key into backend/src/main/resources/application.properties:
        //    tu.api.key=YOUR_APPLICATION_KEY_HERE
        if (isBlank(tuApiKey)) {
            return LoginResponse.error("Student not found in local database and TU Application-Key is not configured yet");
        }

        try {
            // Prepare HTTP Headers with the required TU Application-Key
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Application-Key", tuApiKey); // <-- Uses the Application-Key from application.properties

            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // Build the full request URL: https://restapi.tu.ac.th/api/v2/profile/std/info/?id={studentId}
            String requestUrl = tuApiUrl + studentId;

            // Send GET request to TU REST API and map response to TuApiResponse structure
            ResponseEntity<TuApiResponse> apiResponse = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    httpEntity,
                    TuApiResponse.class
            );

            TuApiResponse tuBody = apiResponse.getBody();

            // Validate response status and student data returned from TU API
            if (tuBody == null || !tuBody.isStatus() || tuBody.getData() == null) {
                return LoginResponse.error("Student ID not found in TU system");
            }

            TuApiResponse.StudentData tuData = tuBody.getData();

            // Verify that the email submitted by the user matches the email from TU API
            if (tuData.getEmail() == null || !tuData.getEmail().equalsIgnoreCase(email)) {
                return LoginResponse.error("Email does not match TU student profile");
            }

            // =========================================================================
            // STEP 3: Save verified student data into local database for future logins
            // =========================================================================
            User newUser = new User(
                    studentId,
                    tuData.getEmail(),
                    tuData.getType(),
                    tuData.getStatusname(),
                    tuData.getPrefixname(),
                    tuData.getDisplayname_th(),
                    tuData.getDisplayname_en(),
                    tuData.getFaculty(),
                    tuData.getDepartment()
            );

            userRepository.save(newUser);
            return LoginResponse.success();

        } catch (Exception e) {
            return LoginResponse.error("Failed to verify with TU API: " + e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
