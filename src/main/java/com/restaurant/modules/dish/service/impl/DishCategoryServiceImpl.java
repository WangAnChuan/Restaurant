package com.restaurant.modules.dish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restaurant.modules.dish.entity.DishCategory;
import com.restaurant.modules.dish.mapper.DishCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory> {

    public List<DishCategory> listByType(Integer type) {
        LambdaQueryWrapper<DishCategory> queryWrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            queryWrapper.eq(DishCategory::getType, type);
        }
        queryWrapper.eq(DishCategory::getStatus, 1);
        queryWrapper.orderByAsc(DishCategory::getSort).orderByDesc(DishCategory::getUpdateTime);
        return this.list(queryWrapper);
    }
}
