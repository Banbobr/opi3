package ru.minibobr.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.minibobr.models.Point;
import ru.minibobr.models.User;
import ru.minibobr.point.PointHitChecker;
import ru.minibobr.repository.PointRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointService {
    private final PointRepository pointRepository;

    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @Transactional
    public Point savePoint(Point point, User user) {
        PointHitChecker checker = new PointHitChecker(point);
        point.setHit(checker.isHit());
        point.setTime(LocalDateTime.now());
        point.setUser(user);
        return pointRepository.save(point);
    }

    public List<Point> getUserPoints(User user) {
        return pointRepository.findByUserOrderByTimeDesc(user);
    }

    public Page<Point> getUserPointsPaginated(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return pointRepository.findByUserOrderByTimeDesc(user, pageable);
    }

    @Transactional
    public void clearUserPoints(User user) {
        pointRepository.deleteByUser(user);
    }
}
