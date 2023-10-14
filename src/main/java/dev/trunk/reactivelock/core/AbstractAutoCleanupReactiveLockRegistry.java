package dev.trunk.reactivelock.core;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This provides functionalities to manipulate reactive locks and will auto evict reactive lock if they have not been used for a specified duration
 * @author Trunk Xiong
 * @since 1.0
 */
@Slf4j
public abstract class AbstractAutoCleanupReactiveLockRegistry implements ReactiveLockRegistry {
    private final Map<Integer, ReactiveLock> lockRegistry = new ConcurrentHashMap<>();
    private final int strip;

    protected AbstractAutoCleanupReactiveLockRegistry(int strip) {
        this.strip = strip;
    }


    @Override
    public ReactiveLock getLock(Object lockKey) {
        return lockRegistry.computeIfAbsent(lockKey.hashCode() % strip, this::newReactiveLock);
    }

    public Flux<Boolean> clean(long now, Duration maxIdleDuration) {
        return Flux.fromIterable(this.lockRegistry.entrySet())
                .filter(entry -> now - entry.getValue().getLatestLockedTime() > maxIdleDuration.toMillis())
                .flatMap(entry -> entry.getValue().isLocked().filter(isLocked -> !isLocked).doOnNext(isLocked -> {
                    log.info("Delete registry: {}", entry.getKey());
                    this.lockRegistry.remove(entry.getKey());
                }).onErrorResume(throwable -> {
                    log.error("Auto remove unused locks: [{}], occur exception: {}", entry, throwable);
                    return Mono.empty();
                }));
    }


    /**
     * create a new reactive lock
     * @param lockKey - the lock key
     * @return a reactive lock
     */
    protected abstract ReactiveLock newReactiveLock(Object lockKey);
}
