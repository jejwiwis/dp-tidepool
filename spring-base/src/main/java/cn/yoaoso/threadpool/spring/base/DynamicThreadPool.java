package cn.yoaoso.threadpool.spring.base;

import java.lang.annotation.*;

/**
 * @author Drayd
 * @description 动态线程池注解
 * @create 2026-01-18 02:44
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicThreadPool {
}
