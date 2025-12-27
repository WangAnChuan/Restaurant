package com.restaurant.modules.dish.dto;

import jakarta.validation.constraints.*; // Use jakarta for Spring Boot 3
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DishDTO implements Serializable {
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotNull(message = "Category required")
    private Long categoryId;

    @NotNull(message = "Price required")
    @Min(value = 0, message = "Price must be >= 0")
    private BigDecimal price;

    private String code;

    private String imageUrl;

    private String description;
    
    private String ingredients;

    private Integer status;
    
    private Integer sort;
}
