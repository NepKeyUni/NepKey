package com.nepkey.backend;

import com.nepkey.backend.dto.AssignmentDTO;
import com.nepkey.backend.dto.CourseDTO;
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

            logger.info("Kérés küldése az URL-re: {}", coursesUrl);
            ResponseEntity<List> coursesResponse = restTemplate.exchange(
                    coursesUrl,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> courses = (List<Map<String, Object>>) coursesResponse.getBody();
            if (courses == null || courses.isEmpty()) {
                logger.info("Nincsenek kurzusok.");
                return new ArrayList<>();
            }

            // Összes feladat összegyűjtése
            List<AssignmentDTO> allAssignments = new ArrayList<>();

            for (Map<String, Object> course : courses) {
                Long courseId = Long.valueOf(String.valueOf(course.get("id")));
                String courseName = (String) course.get("name");
                if (courseName == null) {
                    courseName = "Ismeretlen kurzus";
                }

                String assignmentsUrl = "https://canvas.elte.hu/api/v1/courses/" + courseId + "/assignments";
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(assignmentsUrl)
                        .queryParam("per_page", "100");

                logger.info("Kérés küldése az URL-re: {}", builder.toUriString());
                try {
                    ResponseEntity<List> response = restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.GET,
                            entity,
                            List.class
                    );

                    List<Map<String, Object>> assignments = (List<Map<String, Object>>) response.getBody();
                    if (assignments != null) {
                        for (Map<String, Object> assignment : assignments) {
                            AssignmentDTO assignmentDTO = new AssignmentDTO();
                            assignmentDTO.setId(Long.valueOf(String.valueOf(assignment.get("id"))));
                            assignmentDTO.setName((String) assignment.get("name"));
                            assignmentDTO.setDueAt((String) assignment.get("due_at"));
                            assignmentDTO.setCourseId(courseId);
                            assignmentDTO.setCourseName(courseName);
                            allAssignments.add(assignmentDTO);
                            logger.info("Feladat hozzáadva: {}, Kurzus: {}", assignment.get("name"), courseName);
                        }
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

    public List<CourseDTO> getCourses(String accessToken) {
        try {
            String coursesUrl = "https://canvas.elte.hu/api/v1/courses";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info("Kérés küldése az URL-re: {}", coursesUrl);
            ResponseEntity<List> response = restTemplate.exchange(
                    coursesUrl,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> courses = (List<Map<String, Object>>) response.getBody();
            List<CourseDTO> courseDTOs = new ArrayList<>();
            if (courses != null) {
                for (Map<String, Object> course : courses) {
                    CourseDTO courseDTO = new CourseDTO();
                    courseDTO.setId(Long.valueOf(String.valueOf(course.get("id"))));
                    courseDTO.setName((String) course.get("name"));
                    if (courseDTO.getName() == null) {
                        courseDTO.setName("Ismeretlen kurzus");
                    }
                    courseDTOs.add(courseDTO);
                }
            }
            return courseDTOs;

        } catch (Exception e) {
            logger.error("Váratlan hiba a kurzusok lekérése közben: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch courses: " + e.getMessage());
        }
    }
}