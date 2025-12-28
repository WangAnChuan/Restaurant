package com.restaurant.modules.reservation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.modules.reservation.entity.RestaurantTable;
import com.restaurant.modules.reservation.service.impl.RestaurantTableServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table")
public class RestaurantTableController {

    @Autowired
    private RestaurantTableServiceImpl tableService;

    /**
     * Get all available tables (for customers)
     */
    @GetMapping("/list")
    public Result<List<RestaurantTable>> list() {
        LambdaQueryWrapper<RestaurantTable> query = new LambdaQueryWrapper<>();
        query.eq(RestaurantTable::getStatus, 1)
                .orderByAsc(RestaurantTable::getTableNumber);
        return Result.success(tableService.list(query));
    }

    /**
     * Get paginated table list (for admin)
     */
    @GetMapping("/page")
    public Result<Page<RestaurantTable>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<RestaurantTable> page = new Page<>(current, size);
        LambdaQueryWrapper<RestaurantTable> query = new LambdaQueryWrapper<>();
        query.orderByAsc(RestaurantTable::getTableNumber);
        return Result.success(tableService.page(page, query));
    }

    /**
     * Add new table (admin only)
     */
    @PostMapping
    public Result<Boolean> save(@RequestBody RestaurantTable table) {
        return Result.success(tableService.save(table));
    }

    /**
     * Update table (admin only)
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody RestaurantTable table) {
        return Result.success(tableService.updateById(table));
    }
}
