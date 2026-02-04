package com.restaurant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

// 4. WebMvcConfig.java - Web MVC 配置
// 作用：配置静态资源映射
// 功能：让上传的文件可以通过 URL 访问
// 映射：/uploads/** → uploads/ 目录

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录下的 uploads 目录
        String userDir = System.getProperty("user.dir");
        File uploadsDir = new File(userDir, "uploads");

        // 确保目录存在
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        // 构建正确的 file: URI (必须以 / 结尾)
        String uploadPath = uploadsDir.toURI().toString();
        System.out.println("静态资源映射: /uploads/** -> " + uploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCacheControl(org.springframework.http.CacheControl.maxAge(1, java.util.concurrent.TimeUnit.HOURS)
                        .cachePublic());
    }
}
