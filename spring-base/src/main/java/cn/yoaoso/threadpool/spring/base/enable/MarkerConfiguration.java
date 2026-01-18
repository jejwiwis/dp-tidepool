package cn.yoaoso.threadpool.spring.base.enable;

import org.springframework.context.annotation.Bean;

/**
 * @author Drayd
 * @description 标记配置类
 * @create 2026-01-18 17:17
 */
public class MarkerConfiguration {
    @Bean
    public Marker dynamicThreadPoolMarkerBean() {
        return new Marker();
    }

    /**
     * 标记类
     * 可用于条件装配（@ConditionalOnBean 等）中作为存在性的判断依据
     * <p>
     * 作者：马丁
     * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
     * 开发时间：2025-04-23
     */
    public class Marker {

    }
}
