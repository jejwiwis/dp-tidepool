package cn.yoaoso.threadpool.core.monitor.service;

import cn.yoaoso.threadpool.core.config.BootstrapConfigProperties;
import cn.yoaoso.threadpool.core.monitor.dto.ThreadPoolAlarmNotifyDTO;
import cn.yoaoso.threadpool.core.monitor.dto.ThreadPoolConfigChangeDTO;
import cn.yoaoso.threadpool.core.monitor.dto.WebThreadPoolConfigChangeDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Drayd
 * @description 通知调度器，用于统一管理和路由各类通知发送器（如钉钉、飞书、企业微信等）
 * @create 2026-01-19 14:44
 */
public class NotifierDispatcher implements NotifierService {
    private static final Map<String, NotifierService> NOTIFIER_SERVICE_MAP = new HashMap<>();

    static {
        //TODO 在简单工厂中注册不同的通知实现
//        NOTIFIER_SERVICE_MAP.put("DING", new DingTalkMessageService());
        /**
         * 后续可以轻松扩展其他通知渠道
         * NOTIFIER_SERVICE_MAP.put("WECHAT", new WeChatMessageService());
         * NOTIFIER_SERVICE_MAP.put("EMAIL", new EmailMessageService());
         */
    }

    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChange) {
        getNotifierService().ifPresent(service -> service.sendChangeMessage(configChange));
    }

    @Override
    public void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange) {
        getNotifierService().ifPresent(service -> service.sendWebChangeMessage(configChange));
    }

    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        getNotifierService().ifPresent(service -> {
            // 频率检查
            boolean allowSend = AlarmRateLimiter.allowAlarm(
                    alarm.getThreadPoolId(),
                    alarm.getAlarmType(),
                    alarm.getInterval()
            );

            // 满足频率发送告警
            if (allowSend) {
                service.sendAlarmMessage(alarm.resolve());
            }
        });
    }

    /**
     * 根据配置获取对应的通知服务实现
     * 简单工厂模式的核心方法
     */
    private Optional<NotifierService> getNotifierService() {
        return Optional.ofNullable(BootstrapConfigProperties.getInstance().getNotifyPlatforms())
                .map(BootstrapConfigProperties.NotifyPlatformsConfig::getPlatform)
                .map(platform -> NOTIFIER_SERVICE_MAP.get(platform));
    }
}
