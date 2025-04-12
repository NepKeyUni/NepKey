package com.nepkey.backend;

import com.nepkey.backend.dto.AssignmentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CanvasService {

    private static final Logger logger = LoggerFactory.getLogger(CanvasService.class);

    @Autowired
    private RestTemplate restTemplate;

    public List<AssignmentDTO> getCalendarEvents(String accessToken) {
        try {
            // Kurzusok lekérdezése
            String coursesUrl = "https://canvas.elte.hu/api/v1/courses";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(coursesUrl)
                    .queryParam("per_page", "100");

            logger.info("Kérés küldése az URL-re: {}", builder.toUriString());
            ResponseEntity<List> coursesResponse = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> courses = (List<Map<String, Object>>) coursesResponse.getBody();
            if (courses == null || courses.isEmpty()) {
                logger.info("Nincsenek kurzusok.");
                return new ArrayList<>();
            }

            // Összes feladat összegyűjtése csak a 2024/25/2 kurzusokból
            List<AssignmentDTO> allAssignments = new ArrayList<>();

            for (Map<String, Object> course : courses) {
                String courseName = (String) course.get("name");
                if (courseName == null || !courseName.contains("2024/25/2")) {
                    logger.info("Kurzus kihagyva, nem 2024/25/2: {}", courseName);
                    continue; // Csak 2024/25/2 kurzusokat dolgozunk fel
                }

                Long courseId = Long.valueOf(String.valueOf(course.get("id")));
                logger.info("2024/25/2 kurzus feldolgozása: {} (ID: {})", courseName, courseId);

                String assignmentsUrl = "https://canvas.elte.hu/api/v1/courses/" + courseId + "/assignments";
                UriComponentsBuilder assignmentsBuilder = UriComponentsBuilder.fromHttpUrl(assignmentsUrl)
                        .queryParam("per_page", "100");

                logger.info("Kérés küldése az URL-re: {}", assignmentsBuilder.toUriString());
                try {
                    ResponseEntity<List> response = restTemplate.exchange(
                            assignmentsBuilder.toUriString(),
                            HttpMethod.GET,
                            entity,
                            List.class
                    );

                    List<Map<String, Object>> assignments = (List<Map<String, Object>>) response.getBody();
                    if (assignments != null) {
                        for (Map<String, Object> assignment : assignments) {
                            logger.debug("Nyers feladat adat: {}", assignment);
                            AssignmentDTO assignmentDTO = new AssignmentDTO();
                            assignmentDTO.setId(Long.valueOf(String.valueOf(assignment.get("id"))));
                            assignmentDTO.setName((String) assignment.get("name"));
                            String dueAt = (String) assignment.get("due_at");
                            assignmentDTO.setDueAt(dueAt);
                            assignmentDTO.setCourseName(courseName); // Kurzusnév hozzáadása
                            if (dueAt == null) {
                                logger.warn("Null due_at a feladatnál: {} (Kurzus: {})", assignment.get("name"), courseName);
                            } else {
                                logger.info("Feladat: {}, due_at: {}", assignment.get("name"), dueAt);
                            }
                            allAssignments.add(assignmentDTO);
                        }
                    } else {
                        logger.info("Nincsenek feladatok a kurzushoz: {}", courseName);
                    }
                } catch (Exception e) {
                    logger.warn("Nem sikerült lekérni a feladatokat a kurzushoz {}: {}", courseId, e.getMessage());
                }
            }

            return allAssignments;

        } catch (Exception e) {
            logger.error("Váratlan hiba: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch calendar events: " + e.getMessage());
        }
    }
}