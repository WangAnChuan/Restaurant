package com.restaurant.modules.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.modules.dish.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
