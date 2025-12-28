package com.restaurant.modules.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tableId;
    private String customerName;
    private String customerPhone;
    private LocalDate reservationDate;
    private String reservationTime; // Time slot like "12:00"
    private Integer guestCount;
    private Integer status; // 0:Pending 1:Confirmed 2:Completed 3:Cancelled
    private String remark;
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
