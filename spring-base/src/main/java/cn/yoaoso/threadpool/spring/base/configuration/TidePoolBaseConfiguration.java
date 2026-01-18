package cn.yoaoso.threadpool.spring.base.configuration;

import cn.yoaoso.threadpool.core.config.BootstrapConfigProperties;
import cn.yoaoso.threadpool.spring.base.support.ApplicationContextHolder;
import cn.yoaoso.threadpool.spring.base.support.TidePoolBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

/**
 * @author Drayd
 * @description 动态线程池基础Spring配置类
 * @create 2026-01-18 18:09
 */
public class TidePoolBaseConfiguration {
    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @DependsOn("applicationContextHolder")
    public TidePoolBeanPostProcessor tidePoolBeanPostProcessor(BootstrapConfigProperties properties) {
        return new TidePoolBeanPostProcessor(properties);
    }


}
