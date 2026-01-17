package cn.yoaoso.threadpool.core.toolkit;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;



import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Drayd
 * @description 用于构建自定义线程工厂的建造者类，支持设置线程名、优先级、是否为守护线程等属性
 * @create 2026-01-18 01:27
 */

public class ThreadFactoryBuilder {

    /**
     * 基础线程工厂，默认使用 Executors.defaultThreadFactory()
     */
    private ThreadFactory backingThreadFactory;

    /**
     * 线程名前缀，如 "onethread-"，线程名形如：onethread-1
     */
    private String namePrefix;

    /**
     * 是否为守护线程，默认 false
     */
    private Boolean daemon;

    /**
     * 线程优先级（1~10）
     */
    private Integer priority;

    /**
     * 未捕获异常处理器
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * 创建 ThreadFactoryBuilder 实例
     */
    public static ThreadFactoryBuilder builder() {
        return new ThreadFactoryBuilder();
    }

    public ThreadFactoryBuilder threadFactory(ThreadFactory backingThreadFactory) {
        this.backingThreadFactory = backingThreadFactory;
        return this;
    }

    public ThreadFactoryBuilder namePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    public ThreadFactoryBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadFactoryBuilder priority(int priority) {
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("The thread priority must be between 1 and 10.");
        }
        this.priority = priority;
        return this;
    }

    public ThreadFactoryBuilder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        this.uncaughtExceptionHandler = handler;
        return this;
    }

    /**
     * 构建线程工厂实例
     */
    public ThreadFactory build() {
        //1. 确定底层的造线程工人，如果没指定，就用 JDK 默认的
        final ThreadFactory factory = (this.backingThreadFactory != null) ? this.backingThreadFactory : Executors.defaultThreadFactory();

        //2. 强校验：名字前缀不能为空。这是很好的实践，强制要求给线程起名。
        Assert.notEmpty(namePrefix, "The thread name prefix cannot be empty or an empty string.");

        // 3. 计数器：闭包捕获。
        // 注意：这个 AtomicLong 是在 build() 调用时创建的。
        // 这意味着每个 build 出来的 Factory 都有自己独立的计数器，从 0 开始。
        final AtomicLong count = (StrUtil.isNotBlank(namePrefix)) ? new AtomicLong(0) : null;

        //lambda表达式
        return runnable -> {
            Thread thread = factory.newThread(runnable);

            if (count != null) {
                thread.setName(namePrefix + count.getAndIncrement());
            }

            if (daemon != null) {
                thread.setDaemon(daemon);
            }

            if (priority != null) {
                thread.setPriority(priority);
            }

            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }

            return thread;
        };
    }
}
