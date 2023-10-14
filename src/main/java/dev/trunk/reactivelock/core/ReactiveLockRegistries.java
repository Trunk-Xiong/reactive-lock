package dev.trunk.reactivelock.core;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import dev.trunk.reactivelock.properties.ReactiveLockProperties;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * This holds a {@link ReactiveLockRegistry} for each entity
 *
 * @author Trunk Xiong
 * @since 1.0
 */
public class ReactiveLockRegistries implements InitializingBean, DisposableBean {
    private final Scheduler lockEvictionScheduler = Schedulers.newSingle("reactive-lock-evictor");
    private Disposable lockEvictionDisposable;
    @Getter
    private final boolean lockEnforced;
    private final Map<String, ReactiveLockRegistry> reactiveLockRegistryMap;
    private final ReactiveLockProperties reactiveLockProperties;


    public ReactiveLockRegistries(Collection<String> entities, ReactiveLockProperties reactiveLockProperties) {
        this.reactiveLockProperties = reactiveLockProperties;
        this.lockEnforced = reactiveLockProperties.isEnforced();
        ImmutableMap.Builder<String, ReactiveLockRegistry> builder = ImmutableMap.builder();
        entities.forEach(entity -> builder.put(entity, new DefaultReactiveLockRegistry(reactiveLockProperties.getLockStrips())));
        reactiveLockRegistryMap = builder.build();
    }

    @Override
    public void destroy() {
        lockEvictionDisposable = Flux.interval(reactiveLockProperties.getMaxIdleDuration(), lockEvictionScheduler)
                .flatMap(value -> {
                    long now = System.currentTimeMillis();
                    return Flux.fromIterable(reactiveLockRegistryMap.values()).flatMap(reactiveLockRegistry -> reactiveLockRegistry.clean(now, reactiveLockProperties.getMaxIdleDuration()));
                }).subscribe();
    }

    @Override
    public void afterPropertiesSet() {
        if (Objects.nonNull(this.lockEvictionDisposable) && !lockEvictionDisposable.isDisposed()) {
            lockEvictionDisposable.dispose();
        }
        if (!this.lockEvictionScheduler.isDisposed()) {
            this.lockEvictionScheduler.dispose();
        }
    }
}
