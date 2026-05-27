package ru.minibobr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.minibobr.models.Point;
import ru.minibobr.models.User;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByUserOrderByTimeDesc(User user);
    Page<Point> findByUserOrderByTimeDesc(User user, Pageable pageable);
    void deleteByUser(User user);
}