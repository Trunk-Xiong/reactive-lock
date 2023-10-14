package dev.trunk.reactivelock.core;

import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * This defines common functionalities of an entity registered with reactive locks
 * @author Trunk Xiong
 * @since 1.0
 */
public interface ReactiveLockRegistry {
    /**
     * get a lock by lock key
     * @param lockKey
     * @return reactive lock
     */
    ReactiveLock getLock(Object lockKey);
    Flux<Boolean> clean(long now, Duration idleDuration);
}
