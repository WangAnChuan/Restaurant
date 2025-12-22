package com.restaurant.modules.account.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.modules.account.entity.AccountRecord;
import com.restaurant.modules.account.service.impl.AccountRecordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountRecordController {

    @Autowired
    private AccountRecordServiceImpl accountRecordService;

    @GetMapping("/page")
    public Result<Page<AccountRecord>> page(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer type) {
        Page<AccountRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<AccountRecord> query = new LambdaQueryWrapper<>();
        query.eq(type != null, AccountRecord::getType, type);
        if (startDate != null && endDate != null) {
            query.between(AccountRecord::getTransactionDate, startDate, endDate);
        }
        query.orderByDesc(AccountRecord::getTransactionDate);
        return Result.success(accountRecordService.page(page, query));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody AccountRecord accountRecord) {
        return Result.success(accountRecordService.save(accountRecord));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody AccountRecord accountRecord) {
        return Result.success(accountRecordService.updateById(accountRecord));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(accountRecordService.removeById(id));
    }
}
