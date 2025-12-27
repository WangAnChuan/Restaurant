package com.restaurant.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.modules.system.mapper.SysUserMapper;
import com.restaurant.modules.system.mapper.SysRoleMapper;
import com.restaurant.modules.system.mapper.SysUserRoleMapper;
import com.restaurant.modules.system.entity.SysUser;
import com.restaurant.modules.system.entity.SysRole;
import com.restaurant.modules.system.entity.SysUserRole;
import com.restaurant.modules.account.mapper.CategoryMapper;
import com.restaurant.modules.account.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

// 5. DbInitializer.java - 数据库初始化器
// 作用：应用启动时自动初始化数据
// 初始化内容：
//        3 个角色（老板、采购员、访客）
//        3 个用户（admin、buyer、guest）
//        7 个分类（微信、支付宝、现金等）
// 特点：幂等性、自动修复、版本升级

@Component
public class DbInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========== 数据库初始化检查 ==========");

        // 添加image_url字段（如果不存在）
        addImageUrlColumn();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String defaultPassword = encoder.encode("123456");

        // 1. 初始化角色
        initRole(1L, "老板", "BOSS");
        initRole(2L, "采购员", "PURCHASER");
        initRole(3L, "访客", "VISITOR");

        // 2. 初始化用户
        initUser(1L, "admin", defaultPassword, "张老板", 1L);
        initUser(2L, "buyer", defaultPassword, "李采购", 2L);
        initUser(3L, "guest", defaultPassword, "游客", 3L);

        // 3. 初始化分类 - 中文
        // initCategory(1, "微信");
        // initCategory(1, "支付宝");
        // initCategory(1, "现金");
        // initCategory(2, "肉类食材");
        // initCategory(2, "蔬菜食材");
        // initCategory(2, "调味品");
        // initCategory(2, "日用品");

        System.out.println("========== 初始化完成 ==========");
    }

    private void addImageUrlColumn() {
        try {
            // 检查列是否存在
            String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'image_url'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);

            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE `dish` ADD COLUMN `image_url` VARCHAR(500) AFTER `ingredients`");
                System.out.println("添加 image_url 列到 dish 表");
            }
        } catch (Exception e) {
            System.out.println("检查/添加 image_url 列: " + e.getMessage());
        }
    }

    private void initRole(Long id, String name, String code) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            role = new SysRole();
            role.setId(id);
            role.setRoleName(name);
            role.setRoleCode(code);
            sysRoleMapper.insert(role);
            System.out.println("创建角色: " + name);
        }
    }

    private void initUser(Long id, String username, String password, String realName, Long roleId) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            user = new SysUser();
            user.setId(id);
            user.setUsername(username);
            user.setPassword(password);
            user.setRealName(realName);
            user.setStatus(1);
            user.setDeleted(0);
            sysUserMapper.insert(user);
            System.out.println("创建用户: " + username + " / 123456");

            // 分配角色
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(id);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        } else {
            // 验证并修复密码
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches("123456", user.getPassword())) {
                user.setPassword(password);
                sysUserMapper.updateById(user);
                System.out.println("修复用户密码: " + username);
            }
        }
    }

    private void initCategory(Integer type, String name) {
        Category cat = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getType, type)
                        .eq(Category::getName, name));
        if (cat == null) {
            cat = new Category();
            cat.setType(type);
            cat.setName(name);
            categoryMapper.insert(cat);
            System.out.println("创建分类: " + name);
        }
    }
}
