package cn.yoaoso.threadpool.config.nacos.cloud.starter.refresher;

import cn.yoaoso.threadpool.config.common.starter.refresher.AbstractDynamicThreadPoolRefresher;
import cn.yoaoso.threadpool.core.config.BootstrapConfigProperties;
import cn.yoaoso.threadpool.core.executor.support.BlockingQueueTypeEnum;
import cn.yoaoso.threadpool.core.parser.AbstractConfigParser;
import cn.yoaoso.threadpool.core.toolkit.ThreadPoolExecutorBuilder;
import cn.yoaoso.threadpool.spring.base.enable.MarkerConfiguration;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Drayd
 * @description
 * @create 2026-01-19 00:22
 */
@Slf4j(topic = "TidePoolConfigRefresher")
public class NacosCloudRefresherHandler extends AbstractDynamicThreadPoolRefresher {
    private ConfigService configService;

    public NacosCloudRefresherHandler(ConfigService configService, BootstrapConfigProperties properties) {
        super(properties);
        this.configService = configService;
    }

    public void registerListener() throws NacosException {
        // 1. 获取坐标：从本地配置中拿到 dataId 和 group
        BootstrapConfigProperties.NacosConfig nacosConfig = properties.getNacos();
        // 2. 注册监听：告诉 Nacos SDK，“我要监听这个文件的变化”
        configService.addListener(
                nacosConfig.getDataId(),
                nacosConfig.getGroup(),
                new Listener() {
                    //它指定了用哪个线程池来执行当配置发生变化时的回调逻辑。
                    @Override
                    public Executor getExecutor() {
                        return ThreadPoolExecutorBuilder.builder()
                                .corePoolSize(1)
                                .maximumPoolSize(1)
                                .keepAliveTime(9999L)
                                .workQueueType(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                                .threadFactory("clod-nacos-refresher-thread_")
                                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                                .build();
                    }
                    //当你在 Nacos 控制台修改配置并发布后，Nacos 会把最新的配置内容（字符串形式）传给这个方法。
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        refreshThreadPoolProperties(configInfo);
                    }
                });

        log.info("Dynamic thread pool refresher, add nacos cloud listener success. data-id: {}, group: {}", nacosConfig.getDataId(), nacosConfig.getGroup());
    }

}
