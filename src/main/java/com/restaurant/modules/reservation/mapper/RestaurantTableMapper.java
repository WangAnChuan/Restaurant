package com.restaurant.modules.reservation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.modules.reservation.entity.RestaurantTable;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RestaurantTableMapper extends BaseMapper<RestaurantTable> {
}
