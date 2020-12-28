package com.czh.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @Auhtor：陈志华
 * @createTime：2020-09-24 12:23
 * @Description：
 */
//全局配置类--配置跨域请求
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*","null")
                .allowedMethods("GET","POST","PUT","OPTIONS","DELETE")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
