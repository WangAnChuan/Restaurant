package com.restaurant.modules.dashboard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.common.Result;
import com.restaurant.modules.account.entity.AccountRecord;
import com.restaurant.modules.account.service.impl.AccountRecordServiceImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AccountRecordServiceImpl accountRecordService;

    @GetMapping("/stats")
    public Result<StatsVo> getStats() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        // Fetch all records for this month
        List<AccountRecord> records = accountRecordService.list(new LambdaQueryWrapper<AccountRecord>()
                .ge(AccountRecord::getTransactionDate, startOfMonth));

        BigDecimal totalIncome = records.stream()
                .filter(r -> r.getType() == 1)
                .map(AccountRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = records.stream()
                .filter(r -> r.getType() == 2)
                .map(AccountRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StatsVo vo = new StatsVo();
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setNetIncome(totalIncome.subtract(totalExpense));

        return Result.success(vo);
    }

    @GetMapping("/chart")
    public Result<Map<String, BigDecimal>> getChart(@RequestParam String groupBy) {
        // Simple implementation: Fetch all and group by Java (Not efficient for big
        // data, but OK for MVP)
        // groupBy: payment_method, category_name, transaction_date
        List<AccountRecord> records = accountRecordService.list();

        Map<String, BigDecimal> result;
        if ("payment".equals(groupBy)) {
            result = records.stream().collect(Collectors.groupingBy(
                    r -> r.getPaymentMethod() == null ? "Unknown" : r.getPaymentMethod(),
                    Collectors.reducing(BigDecimal.ZERO, AccountRecord::getAmount, BigDecimal::add)));
        } else if ("category".equals(groupBy)) {
            result = records.stream().collect(Collectors.groupingBy(
                    r -> r.getCategoryName() == null ? "Uncategorized" : r.getCategoryName(),
                    Collectors.reducing(BigDecimal.ZERO, AccountRecord::getAmount, BigDecimal::add)));
        } else {
            result = records.stream().collect(Collectors.groupingBy(
                    r -> r.getTransactionDate().toString(),
                    Collectors.reducing(BigDecimal.ZERO, AccountRecord::getAmount, BigDecimal::add)));
        }
        return Result.success(result);
    }

    @Data
    public static class StatsVo {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netIncome;
    }
}
