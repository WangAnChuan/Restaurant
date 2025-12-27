package com.restaurant.modules.dish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.modules.dish.entity.DishCategory;
import com.restaurant.modules.dish.service.impl.DishCategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dish/category")
public class DishCategoryController {

    @Autowired
    private DishCategoryServiceImpl dishCategoryService;

    @PostMapping
    public Result<String> save(@RequestBody DishCategory dishCategory) {
        dishCategoryService.save(dishCategory);
        return Result.success("Added successfully");
    }

    @GetMapping("/page")
    public Result<Page<DishCategory>> page(int current, int size) {
        Page<DishCategory> pageInfo = new Page<>(current, size);
        LambdaQueryWrapper<DishCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(DishCategory::getSort);
        dishCategoryService.page(pageInfo, queryWrapper);
        return Result.success(pageInfo);
    }

    @DeleteMapping
    public Result<String> delete(Long ids){
        dishCategoryService.removeById(ids);
        return Result.success("Deleted successfully");
    }

    @PutMapping
    public Result<String> update(@RequestBody DishCategory dishCategory){
        dishCategoryService.updateById(dishCategory);
        return Result.success("Updated successfully");
    }

    @GetMapping("/list")
    public Result<List<DishCategory>> list(DishCategory dishCategory){
        LambdaQueryWrapper<DishCategory> queryWrapper = new LambdaQueryWrapper<>();
        if(dishCategory.getType() != null) {
            queryWrapper.eq(DishCategory::getType, dishCategory.getType());
        }
        queryWrapper.orderByAsc(DishCategory::getSort).orderByDesc(DishCategory::getUpdateTime);
        List<DishCategory> list = dishCategoryService.list(queryWrapper);
        return Result.success(list);
    }
}
