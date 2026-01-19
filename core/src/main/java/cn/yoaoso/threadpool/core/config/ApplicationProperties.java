package cn.yoaoso.threadpool.core.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Drayd
 * @description 应用属性配置
 * @create 2026-01-20 02:11
 */
public class ApplicationProperties {
    /**
     * 应用名
     */
    @Getter
    @Setter
    private static String applicationName;

    /**
     * 环境标识
     */
    @Getter
    @Setter
    private static String activeProfile;
}
