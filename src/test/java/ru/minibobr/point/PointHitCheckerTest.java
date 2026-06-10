package ru.minibobr.point;

import org.junit.jupiter.api.Test;
import ru.minibobr.models.Point;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PointHitCheckerTest {

    private Point point(BigDecimal x, BigDecimal y, BigDecimal r) {
        Point p = new Point();
        p.setX(x);
        p.setY(y);
        p.setR(r);
        return p;
    }

    @Test
    void hitInFirstQuadrantRectangle() {
        PointHitChecker checker = new PointHitChecker(point(
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.valueOf(2)));
        assertTrue(checker.isHit());
    }

    @Test
    void missOutsideFirstQuadrantRectangle() {
        PointHitChecker checker = new PointHitChecker(point(
                BigDecimal.valueOf(3), BigDecimal.valueOf(3), BigDecimal.valueOf(2)));
        assertFalse(checker.isHit());
    }

    @Test
    void hitInSecondQuadrantTriangle() {
        PointHitChecker checker = new PointHitChecker(point(
                BigDecimal.valueOf(-0.3), BigDecimal.valueOf(0.3), BigDecimal.valueOf(2)));
        assertTrue(checker.isHit());
    }

    @Test
    void missInSecondQuadrantTriangle() {
        PointHitChecker checker = new PointHitChecker(point(
                BigDecimal.valueOf(-2), BigDecimal.valueOf(3), BigDecimal.valueOf(2)));
        assertFalse(checker.isHit());
    }

    @Test
    void hitInThirdQuadrantCircle() {
        PointHitChecker checker = new PointHitChecker(point(
                BigDecimal.valueOf(-1), BigDecimal.valueOf(-1), BigDecimal.valueOf(2)));
        assertTrue(checker.isHit());
    }

    @Test
    void missInThirdQuadrantCircle() {
        PointHitChecker checker = new PointHitChecker(point(
                BigDecimal.valueOf(-2), BigDecimal.valueOf(-2), BigDecimal.valueOf(2)));
        assertFalse(checker.isHit());
    }

    @Test
    void missWhenRadiusIsZero() {
        PointHitChecker checker = new PointHitChecker(point(
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO));
        assertFalse(checker.isHit());
    }

    @Test
    void missInFourthQuadrant() {
        PointHitChecker checker = new PointHitChecker(point(
                BigDecimal.ONE, BigDecimal.valueOf(-1), BigDecimal.valueOf(2)));
        assertFalse(checker.isHit());
    }
}