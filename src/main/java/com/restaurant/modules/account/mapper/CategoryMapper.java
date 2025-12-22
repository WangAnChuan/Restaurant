package com.restaurant.modules.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.modules.account.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
