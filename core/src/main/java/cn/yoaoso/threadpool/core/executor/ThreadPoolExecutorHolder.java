package cn.yoaoso.threadpool.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Drayd
 * @description 线程池执行者持有对象
 * @create 2026-01-18 01:13
 */
@Data
@AllArgsConstructor
public class ThreadPoolExecutorHolder {
    /**
     * 线程池唯一标识
     */
    private String threadPoolId;

    /**
     * 线程池
     */
    private ThreadPoolExecutor executor;

    /**
     * 线程池属性参数
     */
    private ThreadPoolExecutorProperties executorProperties;
}
