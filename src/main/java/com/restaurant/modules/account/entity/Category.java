package com.restaurant.modules.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type; // 1:Income 2:Expense
    private Long parentId;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy; // In real app, fill this via MetaObjectHandler
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    private Integer deleted;
}
