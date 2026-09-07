package ru.minibobr.point;

import lombok.RequiredArgsConstructor;
import ru.minibobr.models.Point;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@RequiredArgsConstructor
public class PointHitChecker {
    private final Point point;
    private final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    public boolean isHit() {
        BigDecimal r = point.getR();
        BigDecimal x = point.getX();
        BigDecimal y = point.getY();

        if (x == null || y == null || r == null || r.compareTo(BigDecimal.ZERO) == 0) return false;

        BigDecimal zero = BigDecimal.ZERO;

        // 1. 1 четверть (X >= 0, Y >= 0): прямоугольник
        if (x.compareTo(zero) >= 0 && y.compareTo(zero) >= 0) {
            if (x.compareTo(r) <= 0 && y.compareTo(r) <= 0) {
                return true;
            }
        }

        // 2. 2 четверть (X <= 0; Y >= 0): треугольник
        if (x.compareTo(zero) <= 0 && y.compareTo(zero) >= 0) {
            // Уравнение прямой: y <= x + r/2  =>  y - x <= r/2
            BigDecimal rHalf = r.divide(BigDecimal.valueOf(2), MC);
            if (y.subtract(x).compareTo(rHalf) <= 0) {
                return true;
            }
        }

        // 3. 3 четверть (X <= 0, Y <= 0): четверть круга
        if (x.compareTo(zero) <= 0 && y.compareTo(zero) <= 0) {
            BigDecimal x2 = x.pow(2);
            BigDecimal y2 = y.pow(2);
            BigDecimal sum = x2.add(y2);
            BigDecimal r2 = r.pow(2);
            return sum.compareTo(r2) <= 0;
        }

        return false;
    }
}
