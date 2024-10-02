package org.clever.app.autoconfigure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clever.core.AppBasicsConfig;
import org.clever.core.AppContextHolder;
import org.clever.core.Assert;
import org.clever.core.task.StartupTaskBootstrap;
import org.clever.data.jdbc.JdbcBootstrap;
import org.clever.data.redis.RedisBootstrap;
import org.clever.security.SecurityBootstrap;
import org.clever.task.TaskBootstrap;
import org.clever.task.ext.JsExecutorBootstrap;
import org.clever.web.FilterRegistrar;
import org.clever.web.MvcBootstrap;
import org.clever.web.PathConstants;
import org.clever.web.WebServerBootstrap;
import org.clever.web.filter.*;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.io.IOException;

/**
 * 作者：lizw <br/>
 * 创建时间：2024/10/02 09:04 <br/>
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@Getter
@AllArgsConstructor
public class AppAutoConfiguration {
    private final Environment environment;

    @Bean
    public AppBasicsConfig appBasicsConfig() {
        AppBasicsConfig appBasicsConfig = AppBasicsConfig.create(environment);
        appBasicsConfig.init();
        return appBasicsConfig;
    }

    @Bean
    public JdbcBootstrap jdbcBootstrap(AppBasicsConfig appBasicsConfig) {
        JdbcBootstrap jdbcBootstrap = JdbcBootstrap.create(appBasicsConfig.getRootPath(), environment);
        jdbcBootstrap.init();
        return jdbcBootstrap;
    }

    @Bean
    public RedisBootstrap redisBootstrap() {
        RedisBootstrap redisBootstrap = RedisBootstrap.create(environment);
        redisBootstrap.init();
        return redisBootstrap;
    }

    @Bean(initMethod = "start")
    public StartupTaskBootstrap startupTaskBootstrap(AppBasicsConfig appBasicsConfig) {
        StartupTaskBootstrap startupTaskBootstrap = StartupTaskBootstrap.create(appBasicsConfig.getRootPath(), environment);
        ClassLoader classLoader = AppContextHolder.getBean("hotReloadClassLoader", ClassLoader.class);
        if (classLoader != null) {
            startupTaskBootstrap.setClassLoader(classLoader);
        }
        // startupTaskBootstrap.start();
        return startupTaskBootstrap;
    }

    @Bean(initMethod = "start")
    public TaskBootstrap taskBootstrap(AppBasicsConfig appBasicsConfig) {
        TaskBootstrap taskBootstrap = TaskBootstrap.create(appBasicsConfig.getRootPath(), environment);
        JsExecutorBootstrap jsExecutorBootstrap = JsExecutorBootstrap.create(taskBootstrap.getSchedulerConfig(), environment);
        jsExecutorBootstrap.init();
        // taskBootstrap.start();
        return taskBootstrap;
    }

    @Bean
    public WebServerBootstrap webServerBootstrap(AppBasicsConfig appBasicsConfig) {
        return WebServerBootstrap.create(appBasicsConfig.getRootPath(), environment);
    }

    @Bean
    public MvcBootstrap mvcBootstrap(AppBasicsConfig appBasicsConfig) {
        return MvcBootstrap.create(appBasicsConfig.getRootPath(), environment);
    }

    @DependsOn({"jdbcBootstrap", "redisBootstrap"})
    @Bean
    public SecurityBootstrap securityBootstrap() {
        SecurityBootstrap securityBootstrap = SecurityBootstrap.create(environment);
        SecurityBootstrap.useDefaultSecurity(securityBootstrap.getSecurityConfig());
        return securityBootstrap;
    }

    // --------------------------------------------------------------------------------------------
    // HttpFilter(servlet过滤器链)
    //
    // ApplyConfigFilter (应用web配置)
    // 🡓
    // EchoFilter (请求日志)
    // 🡓
    // ExceptionHandlerFilter (异常处理)
    // 🡓
    // GlobalRequestParamsFilter (获取全局请求参数)
    // 🡓
    // CorsFilter (跨域处理)
    // 🡓
    // MvcHandlerMethodFilter (解析获取MVC的HandlerMethod)
    // 🡓
    // [Security]AuthenticationFilter (身份认证拦截)
    // 🡓
    // [Security]LoginFilter (登录拦截)
    // 🡓
    // [Security]LogoutFilter (登出拦截)
    // 🡓
    // [Security]AuthorizationFilter (权限授权拦截)
    // 🡓
    // StaticResourceFilter (静态资源)
    // 🡓
    // MvcFilter (MVC功能)
    // --------------------------------------------------------------------------------------------

    @Bean
    public FilterRegistrationBean<HttpFilter> applyConfigFilter(AppBasicsConfig appBasicsConfig, WebServerBootstrap webServerBootstrap) {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("ApplyConfigFilter");
        filterBean.setFilter(new FilterAdapter(ApplyConfigFilter.create(appBasicsConfig.getRootPath(), webServerBootstrap.getWebConfig())));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> echoFilter() {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 200);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("EchoFilter");
        filterBean.setFilter(new FilterAdapter(EchoFilter.create(environment)));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> exceptionHandlerFilter() {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 300);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("ExceptionHandlerFilter");
        filterBean.setFilter(new FilterAdapter(ExceptionHandlerFilter.INSTANCE));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> globalRequestParamsFilter() {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 400);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("GlobalRequestParamsFilter");
        filterBean.setFilter(new FilterAdapter(GlobalRequestParamsFilter.INSTANCE));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> corsFilter() {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 500);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("CorsFilter");
        filterBean.setFilter(new FilterAdapter(CorsFilter.create(environment)));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> mvcHandlerMethodFilter(MvcBootstrap mvcBootstrap) {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 600);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("MvcHandlerMethodFilter");
        filterBean.setFilter(new FilterAdapter(mvcBootstrap.getMvcHandlerMethodFilter()));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> authenticationFilter(SecurityBootstrap securityBootstrap) {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 700);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("AuthenticationFilter");
        filterBean.setFilter(new FilterAdapter(securityBootstrap.getAuthenticationFilter()));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> loginFilter(SecurityBootstrap securityBootstrap) {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 800);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("LoginFilter");
        filterBean.setFilter(new FilterAdapter(securityBootstrap.getLoginFilter()));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> logoutFilter(SecurityBootstrap securityBootstrap) {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 900);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("LogoutFilter");
        filterBean.setFilter(new FilterAdapter(securityBootstrap.getLogoutFilter()));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> authorizationFilter(SecurityBootstrap securityBootstrap) {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1000);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("AuthorizationFilter");
        filterBean.setFilter(new FilterAdapter(securityBootstrap.getAuthorizationFilter()));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> staticResourceFilter(AppBasicsConfig appBasicsConfig) {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1100);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("StaticResourceFilter");
        filterBean.setFilter(new FilterAdapter(StaticResourceFilter.create(appBasicsConfig.getRootPath(), environment)));
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<HttpFilter> mvcFilter(MvcBootstrap mvcBootstrap) {
        FilterRegistrationBean<HttpFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1200);
        filterBean.addUrlPatterns(PathConstants.ALL);
        filterBean.setName("MvcFilter");
        filterBean.setFilter(new FilterAdapter(mvcBootstrap.getMvcFilter()));
        return filterBean;
    }

    public static class FilterAdapter extends HttpFilter {
        private final FilterRegistrar.FilterFuc fuc;

        public FilterAdapter(FilterRegistrar.FilterFuc fuc) {
            Assert.notNull(fuc, "fuc 不能为 null");
            this.fuc = fuc;
        }

        @Override
        protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
            fuc.doFilter(new FilterRegistrar.Context(req, res, chain));
        }
    }
}
