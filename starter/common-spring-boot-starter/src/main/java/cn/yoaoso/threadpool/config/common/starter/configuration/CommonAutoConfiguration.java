package cn.yoaoso.threadpool.config.common.starter.configuration;

import cn.yoaoso.threadpool.config.common.starter.refresher.DynamicThreadPoolRefreshListener;
import cn.yoaoso.threadpool.core.monitor.service.NotifierDispatcher;
import cn.yoaoso.threadpool.spring.base.configuration.TidePoolBaseConfiguration;
import cn.yoaoso.threadpool.spring.base.enable.MarkerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import cn.yoaoso.threadpool.core.config.BootstrapConfigProperties;
import org.springframework.core.env.Environment;

/**
 * @author Drayd
 * @description 基于配置中心的公共自动装配配置
 * @create 2026-01-18 15:05
 */
@ConditionalOnBean(MarkerConfiguration.Marker.class)//只有当Marker这个Bean存在是，当前配置类才会被加载
@Import(TidePoolBaseConfiguration.class)
@AutoConfigureAfter(TidePoolBaseConfiguration.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class CommonAutoConfiguration {

    @Bean
    public BootstrapConfigProperties bootstrapConfigProperties(Environment environment){
        // 1. 编程式绑定：手动从 Environment (yml) 中读取配置并填充到对象中
        BootstrapConfigProperties bootstrapConfigProperties= Binder.get(environment)
                .bind(BootstrapConfigProperties.PREFIX, Bindable.of(BootstrapConfigProperties.class))
                .get();
        BootstrapConfigProperties.setInstance(bootstrapConfigProperties);
        return bootstrapConfigProperties;
    }

    @Bean
    public DynamicThreadPoolRefreshListener dynamicThreadPoolRefreshListener(NotifierDispatcher notifierDispatcher) {
        return new DynamicThreadPoolRefreshListener(notifierDispatcher);
    }

}
