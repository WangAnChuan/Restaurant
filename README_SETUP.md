# 开发环境配置指南

## 问题：编译失败，找不到 getter/setter 方法

如果你克隆项目后遇到类似以下错误：
```
找不到符号: 方法 getUsername()
找不到符号: 方法 setPassword(java.lang.String)
```

这是因为项目使用了 **Lombok** 来自动生成 getter/setter 方法，需要进行以下配置：

## 解决方案

### 1. IDE 配置（必须）

#### IntelliJ IDEA
1. 安装 Lombok 插件：
   - `File` → `Settings` → `Plugins`
   - 搜索 "Lombok"
   - 安装并重启 IDEA

2. 启用注解处理：
   - `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - 勾选 `Enable annotation processing`

#### Eclipse
1. 下载 `lombok.jar`:
   - 访问 https://projectlombok.org/download
   - 下载最新版本

2. 运行安装程序：
   ```bash
   java -jar lombok.jar
   ```
   - 选择你的 Eclipse 安装目录
   - 点击 Install/Update

#### VS Code
1. 安装扩展：
   - 安装 "Language Support for Java(TM) by Red Hat"
   - 安装 "Lombok Annotations Support for VS Code"

### 2. Maven 清理并重新编译

```bash
# 清理旧的编译文件
mvn clean

# 重新编译
mvn compile

# 或者直接运行
mvn spring-boot:run
```

### 3. 确认 Lombok 依赖

检查 `pom.xml` 中是否包含：
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

## 项目启动

配置完成后，按照 `README.md` 中的步骤启动项目即可。
