package org.clever.app.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 作者：lizw <br/>
 * 创建时间：2024/10/02 10:35 <br/>
 */
@Configuration
@Slf4j
public class AppWebMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(@NotNull CorsRegistry registry) {

    }

    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {

    }
}
