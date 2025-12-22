package com.restaurant.modules.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("account_record")
public class AccountRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer type;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String paymentMethod;
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
