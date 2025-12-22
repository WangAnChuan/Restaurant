package com.restaurant.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("purchase_list")
public class PurchaseList implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate targetDate;
    private String itemName;
    private String quantity;
    private Integer status; // 0:Pending 1:Done
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
