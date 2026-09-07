package ru.minibobr.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.minibobr.dto.PointRequest;
import ru.minibobr.dto.PointResponse;
import ru.minibobr.models.Point;
import ru.minibobr.models.User;
import ru.minibobr.service.PointJooqService;
import ru.minibobr.service.PointService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/points")
public class PointController {
    private final PointService pointService;
    private final PointJooqService pointJooqService;

    public PointController(PointService pointService, PointJooqService pointJooqService) {
        this.pointService = pointService;
        this.pointJooqService = pointJooqService;
    }

    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    @PostMapping
    public ResponseEntity<?> checkPoint(@Valid @RequestBody PointRequest request, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Point point = new Point();
        point.setX(request.getX());
        point.setY(request.getY());
        point.setR(request.getR());

        Point savedPoint = pointService.savePoint(point, user);
        return ResponseEntity.ok(PointResponse.fromPoint(savedPoint));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(pointJooqService.getUserStats(user.getId()));
    }

    @GetMapping
    public ResponseEntity<?> getPoints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<Point> pointsPage = pointService.getUserPointsPaginated(user, page, size);
        List<PointResponse> responses = pointsPage.getContent().stream()
                .map(PointResponse::fromPoint)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PointsPageResponse(
                responses,
                pointsPage.getTotalElements(),
                pointsPage.getTotalPages(),
                pointsPage.getNumber()
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPoints(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Point> points = pointService.getUserPoints(user);
        List<PointResponse> responses = points.stream()
                .map(PointResponse::fromPoint)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping
    public ResponseEntity<?> clearPoints(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        pointService.clearUserPoints(user);
        return ResponseEntity.ok().build();
    }

    private static class PointsPageResponse {
        public List<PointResponse> content;
        public long totalElements;
        public int totalPages;
        public int currentPage;

        public PointsPageResponse(List<PointResponse> content, long totalElements, int totalPages, int currentPage) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }
    }
}

