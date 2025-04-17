package com.nepkey.backend;

import com.nepkey.backend.dto.AssignmentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/canvas")
@CrossOrigin(origins = "http://localhost:5123")
public class CanvasController {

    private static final Logger logger = LoggerFactory.getLogger(CanvasController.class);

    @Autowired
    private CanvasService canvasService;

    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendar(@RequestHeader("Authorization") String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            List<AssignmentDTO> assignments = canvasService.getCalendarEvents(cleanToken);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error fetching calendar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch calendar events: " + e.getMessage() + "\"}");
        }
    }
}