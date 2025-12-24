package com.restaurant.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.modules.system.entity.SysUser;
import com.restaurant.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// 4. UserDetailsServiceImpl.java - 用户认证服务
// 从数据库加载用户信息
// 查询用户的角色和权限
// 返回 UserDetailsImpl 对象

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Load Roles
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = roleCodes.stream().map(code -> "ROLE_" + code).collect(Collectors.toList());

        return new UserDetailsImpl(user, permissions);
    }
}
