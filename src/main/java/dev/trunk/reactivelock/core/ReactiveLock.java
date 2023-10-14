package dev.trunk.reactivelock.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Repeat;

import javax.validation.constraints.NotNull;
import java.util.function.Function;

/** This defines functionalities of a reactive lock
 * @author Trunk Xiong
 * @since 1.0
 */
public interface ReactiveLock {
    /**
     * Try acquiring a lock once on a mono
     * @param lockResultExecution - execution after locking success or locking failure
     * @return a mono
     */
    <T> Mono<T> tryLockMonoThenExecute(@NotNull Function<Boolean, Mono<T>> lockResultExecution);


    /**
     * Try acquiring a lock once on a flux
     * @param lockResultExecution - execution after locking success or locking failre
     * @return a flux
     */
    <T> Flux<T> tryLockFluxThenExecute(@NotNull Function<Boolean, Flux<T>> lockResultExecution);


    /**
     * Try to lock a mono multiple times if not successful
     * @param repeat - repeat specifies how to repeat trying locking if not successful
     * @param lockResultExecution - execution after locking success or locking failure
     * @return a mono
     */
    <T> Mono<T> lockMonoThenExecute(@NotNull Repeat<T> repeat, @NotNull Function<Boolean, Mono<T>> lockResultExecution);

    /**
     * Try to lock a flux multiple times if not successful
     * @param repeat - repeat specifies how to repeat trying locking if not successful
     * @param lockResultExecution - execution after locking success or locking failure
     * @return a flux
     */
    <T> Flux<T> lockFluxThenExecute(@NotNull Repeat<T> repeat, @NotNull Function<Boolean, Flux<T>> lockResultExecution);

    /**
     * get the latest time the reactive lock is locked
     * @return a timestamp
     */
    long getLatestLockedTime();

    /**
     * get the state of a reactive lock
     * @return a mono
     */
    Mono<Boolean> isLocked();

}
