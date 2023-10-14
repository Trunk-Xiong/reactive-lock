package dev.trunk.reactivelock.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is a reactive lock implementation by CAS(compare and swap) algorithm
 * @author Trunk Xiong
 * @since 1.0
 */
@Slf4j
@Getter
@Setter
public class DefaultReactiveLock extends AbstractReactiveLock{
    private volatile long latestLockedTime;
    private final AtomicBoolean lockSignal = new AtomicBoolean(false);


    @Override
    protected Mono<Boolean> acquire() {
        return Mono.fromSupplier(() -> lockSignal.compareAndSet(false, true)).map(lockingResult -> {
            if(lockingResult){
                this.latestLockedTime = System.currentTimeMillis();
            }
            return lockingResult;
        });
    }

    @Override
    protected Mono<Boolean> release() {
        return Mono.fromSupplier(() -> lockSignal.compareAndSet(true, false));
    }

    @Override
    public long getLatestLockedTime() {
        return this.latestLockedTime;
    }

    @Override
    public Mono<Boolean> isLocked() {
        return Mono.just(lockSignal.getAcquire());
    }
}
