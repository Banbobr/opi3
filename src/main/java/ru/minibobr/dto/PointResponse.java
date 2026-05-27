package ru.minibobr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.minibobr.models.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointResponse {
    private Long id;
    private BigDecimal x;
    private BigDecimal y;
    private BigDecimal r;
    private LocalDateTime time;
    private boolean hit;
    private String formattedTime;

    public static PointResponse fromPoint(Point point) {
        return new PointResponse(
            point.getId(),
            point.getX(),
            point.getY(),
            point.getR(),
            point.getTime(),
            point.isHit(),
            point.getFormattedTime()
        );
    }
}


