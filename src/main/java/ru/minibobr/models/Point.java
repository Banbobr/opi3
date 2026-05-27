package ru.minibobr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false)
    private BigDecimal x;

    @Column(nullable = false)
    private BigDecimal y;

    @Column(nullable = false)
    private BigDecimal r;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false)
    private boolean hit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public String getFormattedTime() {
        return time.plusHours(3).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}