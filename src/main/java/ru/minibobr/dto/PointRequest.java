package ru.minibobr.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PointRequest {
    @NotNull(message = "{validation.x.required}")
    @DecimalMin(value = "-4", message = "{validation.x.range}")
    @DecimalMax(value = "4", message = "{validation.x.range}")
    private BigDecimal x;

    @NotNull(message = "{validation.y.required}")
    @DecimalMin(value = "-5", message = "{validation.y.range}")
    @DecimalMax(value = "5", message = "{validation.y.range}")
    private BigDecimal y;

    @NotNull(message = "{validation.r.required}")
    @DecimalMin(value = "1", message = "{validation.r.range}")
    @DecimalMax(value = "4", message = "{validation.r.range}")
    private BigDecimal r;
}

