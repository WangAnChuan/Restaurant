package com.restaurant.modules.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.common.Result;
import com.restaurant.modules.purchase.entity.PurchaseList;
import com.restaurant.modules.purchase.service.impl.PurchaseListServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseListServiceImpl purchaseService;

    @GetMapping("/list")
    public Result<List<PurchaseList>> list(@RequestParam(required = false) LocalDate date) {
        LambdaQueryWrapper<PurchaseList> query = new LambdaQueryWrapper<>();
        query.eq(date != null, PurchaseList::getTargetDate, date)
                .orderByDesc(PurchaseList::getTargetDate);
        return Result.success(purchaseService.list(query));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody PurchaseList purchaseList) {
        return Result.success(purchaseService.save(purchaseList));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody PurchaseList purchaseList) {
        return Result.success(purchaseService.updateById(purchaseList));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(purchaseService.removeById(id));
    }
}
