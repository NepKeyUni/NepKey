package com.nepkey.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CanvasService {

    private static final Logger logger = LoggerFactory.getLogger(CanvasService.class);

    @Autowired
    private RestTemplate restTemplate;

    public String getCalendarEvents(String accessToken) {
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
                return "[]";
            }

            // Összes feladat összegyűjtése
            List<Map<String, Object>> allAssignments = new ArrayList<>();
            LocalDate now = LocalDate.now();
            LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
            LocalDate endOfWeek = now.with(DayOfWeek.SUNDAY);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            logger.info("Aktuális hét kezdete: {}, vége: {}", startOfWeek, endOfWeek);

            for (Map<String, Object> course : courses) {
                String courseId = String.valueOf(course.get("id"));
                String assignmentsUrl = "https://canvas.elte.hu/api/v1/courses/" + courseId + "/assignments";
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(assignmentsUrl)
                        .queryParam("per_page", "50");

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
                            String dueDateStr = (String) assignment.get("due_at");
                            logger.info("Feladat: {}, Határidő: {}", assignment.get("name"), dueDateStr);
                            if (dueDateStr != null) {
                                LocalDate dueDate = LocalDate.parse(dueDateStr.substring(0, 10), formatter);
                                logger.info("Határidő dátum: {}", dueDate);
                                if (!dueDate.isBefore(startOfWeek) && !dueDate.isAfter(endOfWeek)) {
                                    allAssignments.add(assignment);
                                    logger.info("Feladat hozzáadva: {}", assignment.get("name"));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Nem sikerült lekérni a feladatokat a kurzushoz {}: {}", courseId, e.getMessage());
                }
            }

            // JSON konverzió
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(allAssignments);
            logger.info("Minden feladat visszaadása: {}", jsonResponse);
            return jsonResponse;

        } catch (HttpClientErrorException e) {
            logger.error("Kliens hiba: Állapot: {}, Válasz: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "{\"error\": \"Kliens hiba: " + e.getMessage() + "\"}";
        } catch (HttpServerErrorException e) {
            logger.error("Szerver hiba: Állapot: {}, Válasz: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "{\"error\": \"Szerver hiba: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            logger.error("Váratlan hiba: {}", e.getMessage(), e);
            return "{\"error\": \"Váratlan hiba: " + e.getMessage() + "\"}";
        }
    }
}