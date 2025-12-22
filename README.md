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

## 如何同步仓库

当远程仓库有新的更新时，团队成员需要同步最新代码：

### 基本同步流程

```bash
# 1. 进入项目目录
cd restaurant

# 2. 拉取最新代码
git pull

# 3. 如果前端代码有更新，重启前端服务
cd ui
# 先停止当前运行的 npm run dev (Ctrl+C)
npm run dev

# 4. 如果后端代码有更新，重启后端服务
# 回到项目根目录
cd ..
# 先停止当前运行的 mvn spring-boot:run (Ctrl+C)
mvn spring-boot:run
```

### 处理本地有修改的情况

如果你本地有未提交的修改：

```bash
# 方式1: 暂存本地修改
git stash          # 暂存本地修改
git pull           # 拉取远程更新
git stash pop      # 恢复本地修改

# 方式2: 先提交本地修改
git add .
git commit -m "your message"
git pull
```

### 查看远程更新内容

```bash
# 查看远程有哪些更新
git fetch origin
git log HEAD..origin/master

# 查看具体文件变化
git diff HEAD origin/master
```

### 注意事项

- **重启服务**: 修改配置文件（如 `pom.xml`、`vite.config.ts`）后必须重启相应服务
- **依赖更新**: 如果 `pom.xml` 或 `package.json` 有变化，需要重新安装依赖
  - 后端: `mvn clean install`
  - 前端: `cd ui && npm install`
