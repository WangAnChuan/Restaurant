package com.restaurant.modules.reservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restaurant.modules.reservation.entity.Reservation;
import com.restaurant.modules.reservation.entity.RestaurantTable;
import com.restaurant.modules.reservation.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> {

    @Autowired
    private RestaurantTableServiceImpl tableService;

    /**
     * Validate reservation before creating
     */
    public void validateReservation(Reservation reservation) {
        // 1. Check business hours (10:00-22:00)
        String timeStr = reservation.getReservationTime();
        int hour = Integer.parseInt(timeStr.split(":")[0]);
        if (hour < 10 || hour >= 22) {
            throw new RuntimeException("预定时间必须在营业时间内（10:00-22:00）");
        }

        // 2. Check if date is not in the past
        if (reservation.getReservationDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("不能预定过去的日期");
        }

        // 3. Check table exists and is available
        RestaurantTable table = tableService.getById(reservation.getTableId());
        if (table == null) {
            throw new RuntimeException("餐桌不存在");
        }
        if (table.getStatus() != 1) {
            throw new RuntimeException("该餐桌暂不可用");
        }

        // 4. Check guest count vs table capacity
        if (reservation.getGuestCount() > table.getCapacity()) {
            throw new RuntimeException("就餐人数超过餐桌容量");
        }

        // 5. Check time conflict (same table, same date, same time)
        LambdaQueryWrapper<Reservation> query = new LambdaQueryWrapper<>();
        query.eq(Reservation::getTableId, reservation.getTableId())
                .eq(Reservation::getReservationDate, reservation.getReservationDate())
                .eq(Reservation::getReservationTime, reservation.getReservationTime())
                .in(Reservation::getStatus, 0, 1); // Pending or Confirmed
        long count = this.count(query);
        if (count > 0) {
            throw new RuntimeException("该时段已被预定，请选择其他时段或餐桌");
        }
    }

    /**
     * Get available tables for specific date, time and guest count
     */
    public List<RestaurantTable> getAvailableTables(LocalDate date, String time, Integer guestCount) {
        // Get all available tables with enough capacity
        LambdaQueryWrapper<RestaurantTable> tableQuery = new LambdaQueryWrapper<>();
        tableQuery.eq(RestaurantTable::getStatus, 1)
                .ge(RestaurantTable::getCapacity, guestCount)
                .orderByAsc(RestaurantTable::getCapacity); // Sort by capacity ascending
        List<RestaurantTable> allTables = tableService.list(tableQuery);

        // Filter out tables that are already reserved at this time
        LambdaQueryWrapper<Reservation> reservationQuery = new LambdaQueryWrapper<>();
        reservationQuery.eq(Reservation::getReservationDate, date)
                .eq(Reservation::getReservationTime, time)
                .in(Reservation::getStatus, 0, 1); // Pending or Confirmed
        List<Reservation> existingReservations = this.list(reservationQuery);

        // Remove reserved tables from available list
        existingReservations
                .forEach(reservation -> allTables.removeIf(table -> table.getId().equals(reservation.getTableId())));

        return allTables;
    }
}
