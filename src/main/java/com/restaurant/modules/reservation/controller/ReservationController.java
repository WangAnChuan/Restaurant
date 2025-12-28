package com.restaurant.modules.reservation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.modules.reservation.entity.Reservation;
import com.restaurant.modules.reservation.entity.RestaurantTable;
import com.restaurant.modules.reservation.service.impl.ReservationServiceImpl;
import com.restaurant.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationServiceImpl reservationService;

    /**
     * Get available tables for specific date, time and guest count
     */
    @GetMapping("/available-tables")
    public Result<List<RestaurantTable>> getAvailableTables(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String time,
            @RequestParam Integer guestCount) {
        try {
            List<RestaurantTable> tables = reservationService.getAvailableTables(date, time, guestCount);
            return Result.success(tables);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Create reservation (for customers)
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody Reservation reservation) {
        try {
            // Set creator ID from current user
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            Long userId = userDetails.getSysUser().getId();
            reservation.setCreateBy(userId);

            // Validate reservation
            reservationService.validateReservation(reservation);

            // Save reservation
            boolean success = reservationService.save(reservation);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Get my reservations (for customers)
     */
    @GetMapping("/my")
    public Result<List<Reservation>> getMyReservations() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long userId = userDetails.getSysUser().getId();
        LambdaQueryWrapper<Reservation> query = new LambdaQueryWrapper<>();
        query.eq(Reservation::getCreateBy, userId)
                .orderByDesc(Reservation::getReservationDate)
                .orderByDesc(Reservation::getReservationTime);
        return Result.success(reservationService.list(query));
    }

    /**
     * Get paginated reservations (for admin)
     */
    @GetMapping("/page")
    public Result<Page<Reservation>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Integer status) {
        Page<Reservation> page = new Page<>(current, size);
        LambdaQueryWrapper<Reservation> query = new LambdaQueryWrapper<>();

        if (date != null) {
            query.eq(Reservation::getReservationDate, date);
        }
        if (status != null) {
            query.eq(Reservation::getStatus, status);
        }

        query.orderByDesc(Reservation::getReservationDate)
                .orderByAsc(Reservation::getReservationTime);

        return Result.success(reservationService.page(page, query));
    }

    /**
     * Update reservation status (for admin)
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            Reservation reservation = reservationService.getById(id);
            if (reservation == null) {
                return Result.error("预定不存在");
            }
            reservation.setStatus(status);
            return Result.success(reservationService.updateById(reservation));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Cancel reservation
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> cancel(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getById(id);
            if (reservation == null) {
                return Result.error("预定不存在");
            }

            // Check permission: only creator or admin can cancel
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            Long userId = userDetails.getSysUser().getId();
            if (!reservation.getCreateBy().equals(userId)) {
                // TODO: check if user is admin
            }

            reservation.setStatus(3); // Cancelled
            return Result.success(reservationService.updateById(reservation));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
