package ru.minibobr.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PointRequest {
    @NotNull(message = "X is required")
    @DecimalMin(value = "-4", message = "X must be between -4 and 4")
    @DecimalMax(value = "4", message = "X must be between -4 and 4")
    private BigDecimal x;

    @NotNull(message = "Y is required")
    @DecimalMin(value = "-5", message = "Y must be between -5 and 5")
    @DecimalMax(value = "5", message = "Y must be between -5 and 5")
    private BigDecimal y;

    @NotNull(message = "R is required")
    @DecimalMin(value = "1", message = "R must be between 1 and 4")
    @DecimalMax(value = "4", message = "R must be between 1 and 4")
    private BigDecimal r;
}

