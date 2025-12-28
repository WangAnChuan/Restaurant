package com.restaurant.modules.dish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.modules.dish.entity.Dish;
import com.restaurant.modules.dish.service.impl.DishServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dish")
public class DishController {

    @Autowired
    private DishServiceImpl dishService;

    // Public API for Visitors
    @GetMapping("/public/list")
    public Result<List<Dish>> publicList() {
        return Result.success(dishService.list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getId)));
    }

    @GetMapping("/page")
    public Result<Page<Dish>> page(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        Page<Dish> page = new Page<>(current, size);
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.like(name != null, Dish::getName, name);
        return Result.success(dishService.page(page, query));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Dish dish) {
        return Result.success(dishService.save(dish));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Dish dish) {
        return Result.success(dishService.updateById(dish));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(dishService.removeById(id));
    }
}
