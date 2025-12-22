package com.restaurant.modules.account.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.common.Result;
import com.restaurant.modules.account.entity.Category;
import com.restaurant.modules.account.service.impl.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<>();
        query.eq(type != null, Category::getType, type);
        return Result.success(categoryService.list(query));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Category category) {
        return Result.success(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(categoryService.removeById(id));
    }
}
