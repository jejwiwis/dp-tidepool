package cn.yoaoso.threadpool.spring.base.support;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.yoaoso.threadpool.core.config.BootstrapConfigProperties;
import cn.yoaoso.threadpool.core.executor.ThreadPoolExecutorProperties;
import cn.yoaoso.threadpool.core.executor.ThreadPoolRegistry;
import cn.yoaoso.threadpool.core.executor.TidePoolExecutor;
import cn.yoaoso.threadpool.core.executor.support.BlockingQueueTypeEnum;
import cn.yoaoso.threadpool.core.executor.support.RejectedPolicyTypeEnum;
import cn.yoaoso.threadpool.spring.base.DynamicThreadPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Drayd
 * @description 动态线程池后置处理器，扫描 Bean 是否为动态线程池，如果是的话进行属性填充和注册
 * @create 2026-01-18 18:14
 */
@Slf4j
@RequiredArgsConstructor
public class TidePoolBeanPostProcessor implements BeanPostProcessor {
    private final BootstrapConfigProperties properties;


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TidePoolExecutor) {
            DynamicThreadPool dynamicThreadPool;
            try {
                // 通过 IOC 容器扫描 Bean 是否存在动态线程池注解
                dynamicThreadPool = ApplicationContextHolder.findAnnotationOnBean(beanName, DynamicThreadPool.class);
                if (Objects.isNull(dynamicThreadPool)) {
                    return bean;
                }
            } catch (Exception ex) {
                log.error("Failed to create dynamic thread pool in annotation mode.", ex);
                return bean;
            }

            TidePoolExecutor tidePoolExecutor = (TidePoolExecutor) bean;
            // 它拿着线程池的 ID（threadPoolId），去 BootstrapConfigProperties（也就是 Nacos/YAML 配置）里找对应的配置项。
            // 如果代码里定义了一个动态线程池，但 Nacos 里没配参数，直接抛异常阻止启动。这是一种**Fail-Fast（快速失败）**机制。
            ThreadPoolExecutorProperties executorProperties = properties.getExecutors()
                    .stream()
                    .filter(each -> Objects.equals(tidePoolExecutor.getThreadPoolId(), each.getThreadPoolId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("The thread pool id does not exist in the configuration."));
            //调用 overrideLocalThreadPoolConfig 方法，用远程配置覆盖本地硬编码的参数。
            overrideLocalThreadPoolConfig(executorProperties, tidePoolExecutor);

            // 注册到动态线程池注册器，后续监控和报警从注册器获取线程池实例。同时，参数动态变更需要依赖 ThreadPoolExecutorProperties 比对是否有边跟
            ThreadPoolRegistry.putHolder(tidePoolExecutor.getThreadPoolId(), tidePoolExecutor, executorProperties);
        }

        return bean;
    }

    /**
     *
     */
    private void overrideLocalThreadPoolConfig(ThreadPoolExecutorProperties executorProperties, TidePoolExecutor tidePoolExecutor) {
        Integer remoteCorePoolSize = executorProperties.getCorePoolSize();
        Integer remoteMaximumPoolSize = executorProperties.getMaximumPoolSize();
        Assert.isTrue(remoteCorePoolSize <= remoteMaximumPoolSize, "remoteCorePoolSize must be smaller than remoteMaximumPoolSize.");

        // 如果不清楚为什么有这段逻辑，可以参考 Hippo4j Issue https://github.com/opengoofy/hippo4j/issues/1063
        int originalMaximumPoolSize = tidePoolExecutor.getMaximumPoolSize();
        // 场景 A：新核心线程数 > 旧最大线程数 -> 先调大 Max，再调大 Core
        // 场景 B：正常情况 -> 先调 Core，再调 Max
        if (remoteCorePoolSize > originalMaximumPoolSize)  {
            tidePoolExecutor.setMaximumPoolSize(remoteMaximumPoolSize);
            tidePoolExecutor.setCorePoolSize(remoteCorePoolSize);
        } else {
            tidePoolExecutor.setCorePoolSize(remoteCorePoolSize);
            tidePoolExecutor.setMaximumPoolSize(remoteMaximumPoolSize);
        }

        // 阻塞队列没有常规 set 方法，所以使用反射赋值
        BlockingQueue workQueue = BlockingQueueTypeEnum.createBlockingQueue(executorProperties.getWorkQueue(), executorProperties.getQueueCapacity());
        // Java 9+ 的模块系统（JPMS）默认禁止通过反射访问 JDK 内部 API 的私有字段，所以需要配置开放反射权限
        // 在启动命令中增加以下参数，显式开放 java.util.concurrent 包
        // IDE 中通过在 VM options 中添加参数：--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
        // 部署的时候，在启动脚本（如 java -jar 命令）中加入该参数：java -jar --add-opens=java.base/java.util.concurrent=ALL-UNNAMED your-app.jar
        ReflectUtil.setFieldValue(tidePoolExecutor, "workQueue", workQueue);

        // 赋值动态线程池其他核心参数
        tidePoolExecutor.setKeepAliveTime(executorProperties.getKeepAliveTime(), TimeUnit.SECONDS);
        tidePoolExecutor.allowCoreThreadTimeOut(executorProperties.getAllowCoreThreadTimeOut());
        tidePoolExecutor. setRejectedExecutionHandler(RejectedPolicyTypeEnum.createPolicy(executorProperties.getRejectedHandler()));
    }

}
