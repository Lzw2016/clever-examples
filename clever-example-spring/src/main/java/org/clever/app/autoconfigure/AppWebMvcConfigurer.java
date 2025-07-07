package org.clever.app.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.clever.core.AppBasicsConfig;
import org.clever.core.ResourcePathUtils;
import org.clever.web.config.CorsConfig;
import org.clever.web.config.StaticResourceConfig;
import org.clever.web.filter.CorsFilter;
import org.clever.web.filter.StaticResourceFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.*;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 作者：lizw <br/>
 * 创建时间：2024/10/02 10:35 <br/>
 */
@Configuration
@AllArgsConstructor
@Getter
public class AppWebMvcConfigurer implements WebMvcConfigurer {
    private final AppBasicsConfig appBasicsConfig;
    private final CorsFilter corsFilter;
    private final StaticResourceFilter staticResourceFilter;

    @Override
    public void addCorsMappings(@NotNull CorsRegistry registry) {
        CorsConfig corsConfig = corsFilter.getCorsConfig();
        if (corsConfig.isEnable()) {
            List<String> pathPattern = Optional.ofNullable(corsConfig.getPathPattern()).orElse(Collections.emptyList());
            for (String path : pathPattern) {
                if (StringUtils.isBlank(path)) {
                    continue;
                }
                //设置允许跨域的路径
                CorsRegistration corsRegistration = registry.addMapping(path);
                if (corsConfig.getAllowedOriginPatterns() != null && !corsConfig.getAllowedOriginPatterns().isEmpty()) {
                    corsRegistration.allowedOriginPatterns(corsConfig.getAllowedOriginPatterns().toArray(new String[0]));
                }
                if (corsConfig.getAllowedOrigins() != null && !corsConfig.getAllowedOrigins().isEmpty()) {
                    corsRegistration.allowedOrigins(corsConfig.getAllowedOrigins().toArray(new String[0]));
                }
                if (corsConfig.getAllowedMethods() != null && !corsConfig.getAllowedMethods().isEmpty()) {
                    corsRegistration.allowedMethods(corsConfig.getAllowedMethods().toArray(new String[0]));
                }
                if (corsConfig.getAllowedHeaders() != null && !corsConfig.getAllowedHeaders().isEmpty()) {
                    corsRegistration.allowedHeaders(corsConfig.getAllowedHeaders().toArray(new String[0]));
                }
                if (corsConfig.getExposedHeaders() != null && !corsConfig.getExposedHeaders().isEmpty()) {
                    corsRegistration.exposedHeaders(corsConfig.getExposedHeaders().toArray(new String[0]));
                }
                if (corsConfig.getAllowCredentials() != null) {
                    corsRegistration.allowCredentials(corsConfig.getAllowCredentials());
                }
                if (corsConfig.getMaxAge() != null) {
                    corsRegistration.maxAge(corsConfig.getMaxAge());
                }
            }
        }
    }

    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
        StaticResourceConfig staticResourceConfig = staticResourceFilter.getStaticResourceConfig();
        if (staticResourceConfig.isEnable()) {
            List<StaticResourceConfig.ResourceMapping> mappings = Optional.ofNullable(staticResourceConfig.getMappings()).orElse(Collections.emptyList());
            for (StaticResourceConfig.ResourceMapping mapping : mappings) {
                String hostedPath = mapping.getHostedPath();
                String location = mapping.getLocation();
                Duration cachePeriod = mapping.getCachePeriod();
                Resource resource = ResourcePathUtils.getResource(appBasicsConfig.getRootPath(), location);
                ResourceHandlerRegistration handle = registry.addResourceHandler(hostedPath)
                    .addResourceLocations(resource)
                    .setCacheControl(CacheControl.noStore())
                    .setUseLastModified(true)
                    .setOptimizeLocations(false);
                if (cachePeriod != null && cachePeriod.toMillis() > 0) {
                    handle.setCacheControl(CacheControl.maxAge(cachePeriod));
                }
            }
        }
    }
}
