package com.restaurant.common;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

// 3. MyBaseMapper.java - 自定义 Mapper 基类
// 作用：提供统一的 Mapper 基类，预留扩展点
// 当前功能：继承 MyBatis-Plus 的 BaseMapper
// 未来扩展：可添加自定义通用方法（批量插入、逻辑删除等）

// Just a marker or common base if needed, currently MP's BaseMapper is enough.
public interface MyBaseMapper<T> extends BaseMapper<T> {
}
