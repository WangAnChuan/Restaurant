package com.restaurant.modules.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.modules.dish.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishCategoryMapper extends BaseMapper<DishCategory> {
}
