package cn.yoaoso.threadpool.config.common.starter.refresher;

import cn.yoaoso.threadpool.core.config.BootstrapConfigProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author Drayd
 * @description 配置中心刷新线程池参数变更事件
 * @create 2026-01-19 02:15
 */
public class ThreadPoolConfigUpdateEvent extends ApplicationEvent {

    @Getter
    @Setter
    private BootstrapConfigProperties bootstrapConfigProperties;

    public ThreadPoolConfigUpdateEvent(Object source, BootstrapConfigProperties bootstrapConfigProperties) {
        super(source);
        this.bootstrapConfigProperties = bootstrapConfigProperties;
    }
}
