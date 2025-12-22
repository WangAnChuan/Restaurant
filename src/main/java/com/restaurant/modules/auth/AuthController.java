package com.restaurant.modules.auth;

import com.restaurant.common.Result;
import com.restaurant.security.JwtUtils;
import com.restaurant.security.UserDetailsImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private com.restaurant.modules.system.mapper.SysUserMapper sysUserMapper;

    @Autowired
    private com.restaurant.modules.system.mapper.SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails.getUsername());

        Map<String, Object> map = new HashMap<>();
        map.put("token", jwt);
        map.put("username", userDetails.getUsername());
        map.put("roles", userDetails.getAuthorities());
        return Result.success(map);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody LoginDto registerDto) {
        if (registerDto.getUsername() == null || registerDto.getPassword() == null) {
            return Result.error("Username and password required");
        }

        // Check if exists
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.restaurant.modules.system.entity.SysUser> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.restaurant.modules.system.entity.SysUser::getUsername, registerDto.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            return Result.error("Username already exists");
        }

        // Create User
        com.restaurant.modules.system.entity.SysUser user = new com.restaurant.modules.system.entity.SysUser();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRealName("Guest");
        user.setStatus(1);
        sysUserMapper.insert(user);

        // Assign Role (3 = Visitor)
        com.restaurant.modules.system.entity.SysUserRole userRole = new com.restaurant.modules.system.entity.SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(3L);
        sysUserRoleMapper.insert(userRole);

        return Result.success("Registration successful");
    }

    @GetMapping("/info")
    public Result<UserDetailsImpl> info() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return Result.success(userDetails);
    }

    @Data
    public static class LoginDto {
        private String username;
        private String password;
    }
}
