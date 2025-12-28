package com.restaurant.modules.dish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal price;
    private String ingredients;
    private String imageUrl;
    private Long categoryId; // New field
    private String code; // New field
    private String description; // New field
    private Integer sort; // New field
    private Integer status; // 1:OnShelf 0:OffShelf

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
