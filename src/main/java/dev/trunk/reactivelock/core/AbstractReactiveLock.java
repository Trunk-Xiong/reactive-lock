package dev.trunk.reactivelock.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Repeat;

import javax.validation.constraints.NotNull;
import java.util.function.Function;

/**
 * This is an abstract implementation of {@link ReactiveLock}
 *
 * @author Trunk Xiong
 * @since 1.0
 */
public abstract class AbstractReactiveLock implements ReactiveLock {

    @Override
    public <T> Mono<T> tryLockMonoThenExecute(@NotNull Function<Boolean, Mono<T>> lockResultExecution) {
        return Mono.usingWhen(
                Mono.just(this),
                reactiveLock -> reactiveLock.acquire().flatMap(lockResultExecution),
                AbstractReactiveLock::release,
                (reactiveLock, err) -> reactiveLock.release(),
                AbstractReactiveLock::release
        );
    }

    @Override
    public <T> Flux<T> tryLockFluxThenExecute(@NotNull Function<Boolean, Flux<T>> lockResultExecution) {
        return Flux.usingWhen(
                Mono.just(this),
                reactiveLock -> reactiveLock.acquire().flatMapMany(lockResultExecution),
                AbstractReactiveLock::release,
                (reactiveLock, err) -> reactiveLock.release(),
                AbstractReactiveLock::release
        );
    }

    @Override
    public <T> Mono<T> lockMonoThenExecute(@NotNull Repeat<T> repeat, @NotNull Function<Boolean, Mono<T>> lockResultExecution) {
        return Mono.usingWhen(
                Mono.just(this),
                reactiveLock -> reactiveLock.acquire().filter(result ->result).repeatWhenEmpty(repeat).defaultIfEmpty(false).flatMap(lockResultExecution),
                AbstractReactiveLock::release,
                (reactiveLock, err) -> reactiveLock.release(),
                AbstractReactiveLock::release
        );
    }

    @Override
    public <T> Flux<T> lockFluxThenExecute(@NotNull Repeat<T> repeat, @NotNull Function<Boolean, Flux<T>> lockResultExecution) {
        return Flux.usingWhen(
                Mono.just(this),
                reactiveLock -> reactiveLock.acquire().filter(result ->result).repeatWhenEmpty(repeat).defaultIfEmpty(false).flatMapMany(lockResultExecution),
                AbstractReactiveLock::release,
                (reactiveLock, err) -> reactiveLock.release(),
                AbstractReactiveLock::release
        );
    }

    /**
     * To acquire the lock
     *
     * @return mono of boolean
     */
    protected abstract Mono<Boolean> acquire();

    /**
     * To release the lock
     *
     * @return mono of boolean
     */
    protected abstract Mono<Boolean> release();
}
