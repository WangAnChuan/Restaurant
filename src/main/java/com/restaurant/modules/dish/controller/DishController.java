package com.restaurant.modules.dish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.modules.dish.dto.DishDTO;
import com.restaurant.modules.dish.entity.Dish;
import com.restaurant.modules.dish.service.impl.DishServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dish")
public class DishController {

    @Autowired
    private DishServiceImpl dishService;

    // Public API for Visitors
    @GetMapping("/public/list")
    public Result<List<Dish>> publicList(@RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.eq(Dish::getStatus, 1);
        query.eq(categoryId != null, Dish::getCategoryId, categoryId);
        query.orderByAsc(Dish::getSort).orderByDesc(Dish::getId);
        return Result.success(dishService.list(query));
    }

    @GetMapping("/page")
    public Result<Page<Dish>> page(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        Page<Dish> page = new Page<>(current, size);
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.like(StringUtils.isNotBlank(name), Dish::getName, name);
        query.eq(categoryId != null, Dish::getCategoryId, categoryId);
        query.eq(status != null, Dish::getStatus, status);
        query.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        return Result.success(dishService.page(page, query));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Validated DishDTO dishDto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDto, dish);
        return Result.success(dishService.save(dish));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody @Validated DishDTO dishDto) {
        if (dishDto.getId() == null) {
            return Result.error("ID required for update");
        }
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDto, dish);
        return Result.success(dishService.updateById(dish));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(dishService.removeById(id));
    }
}
