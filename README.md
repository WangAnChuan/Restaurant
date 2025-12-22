# 餐厅管理系统 (Restaurant Management System)

这是一个基于 Spring Boot 3 + Vue 3 的餐厅管理系统全栈项目。

## 项目技术栈

*   **后端**: Java 17, Spring Boot 3.2.1, MyBatis-Plus, MySQL 8.0
*   **前端**: Vue 3, Vite, Element-Plus, TypeScript, Pinia

## 启动指南

### 1. 环境准备

请确保本地已安装：
*   **Java**: JDK 17+
*   **Node.js**: v18+
*   **MySQL**: v8.0+

### 2. 数据库配置

1.  创建一个名为 `restaurant_db` 的 MySQL 数据库。
2.  执行初始化 SQL 脚本：
    *   `src/main/resources/sql/init.sql` (表结构与初始数据)
3.  **修改配置**：
    打开 `src/main/resources/application.yml`，修改数据库用户名和密码：
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/restaurant_db?...
        username: root      # 你的数据库用户名
        password: 123456    # 你的数据库密码
    ```

### 3. 启动项目

#### 启动后端
在项目根目录下执行：
```bash
mvn spring-boot:run
```

#### 启动前端
进入 `ui` 目录并启动：
```bash
cd ui
npm run dev
```

### 4. 访问

浏览器打开：[http://localhost:3000](http://localhost:3000)

*   **默认管理员账号**: `admin` / `123456`
*   **默认老板账号**: `boss` / `123456`
