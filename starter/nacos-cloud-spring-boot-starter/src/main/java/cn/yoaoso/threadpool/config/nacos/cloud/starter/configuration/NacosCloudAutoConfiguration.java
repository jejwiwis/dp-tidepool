package cn.yoaoso.threadpool.config.nacos.cloud.starter.configuration;

import cn.yoaoso.threadpool.config.nacos.cloud.starter.refresher.NacosCloudRefresherHandler;
import cn.yoaoso.threadpool.core.config.BootstrapConfigProperties;
import cn.yoaoso.threadpool.spring.base.enable.MarkerConfiguration;
import com.alibaba.cloud.nacos.NacosConfigManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author Drayd
 * @description
 * @create 2026-01-19 00:48
 */
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class NacosCloudAutoConfiguration {

    @Bean
    public NacosCloudRefresherHandler nacosCloudRefresherHandler(NacosConfigManager nacosConfigManager, BootstrapConfigProperties properties) {
        return new NacosCloudRefresherHandler(nacosConfigManager.getConfigService(), properties);
    }
}
