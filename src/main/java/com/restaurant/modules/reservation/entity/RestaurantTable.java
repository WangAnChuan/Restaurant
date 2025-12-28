package com.restaurant.modules.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("restaurant_table")
public class RestaurantTable implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tableNumber;
    private String tableType; // ROUND_TABLE or SQUARE_TABLE
    private Integer capacity;
    private Integer status; // 1:Available 0:Unavailable

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
