import dev.trunk.reactivelock.core.DefaultReactiveLock;
import dev.trunk.reactivelock.core.DefaultReactiveLockRegistry;
import dev.trunk.reactivelock.core.ReactiveLockRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Repeat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link DefaultReactiveLock}
 *
 * @author Trunk Xiong
 * @since 1.0
 */
@RunWith(SpringRunner.class)
public class DefaultReactiveLockTest {
    private final ReactiveLockRegistry defaultReactiveLockRegistry = new DefaultReactiveLockRegistry(100);

    @Test
    public void testTryLockMonoOnceByParallelThreads() {
        Integer result = Flux.range(0, 5).flatMap(value -> this.defaultReactiveLockRegistry.getLock("testTryLockMonoOnceByParallelThreads")
                .tryLockMonoThenExecute(lockResult -> {
                            if (lockResult) {
                                return Mono.just(1).delayElement(Duration.ofSeconds(1));
                            } else {
                                return Mono.just(0).delayElement(Duration.ofSeconds(5));
                            }
                        }
                )).reduce(Integer::sum).block();
        assertEquals(1, result.intValue());
    }

    @Test
    public void testTryLockMonoWithDurationByParallelThreads() {
        AtomicInteger lockSuccessCnt = new AtomicInteger(0);
        Flux.range(0, 3).flatMap(value -> this.defaultReactiveLockRegistry.getLock("testTryLockMonoWithDurationByParallelThreads")
                .lockMonoThenExecute(
                        createRepeatFunction(Duration.ofMillis(100), Duration.ofMillis(10)),
                        lockResult -> {
                            if (lockResult) {
                                lockSuccessCnt.getAndIncrement();
                            }
                            return Mono.empty();
                        }
                )).then().block();
        assertEquals(3, lockSuccessCnt.intValue());
    }

    @Test
    public void testTryLockFluxOnceByParallelThreads() {
        Integer result = Flux.range(0, 5).flatMap(value -> this.defaultReactiveLockRegistry.getLock("testTryLockFluxOnceByParallelThreads")
                .tryLockFluxThenExecute(lockResult -> {
                            if (lockResult) {
                                return Flux.just(1, 2).delayElements(Duration.ofSeconds(1));
                            } else {
                                return Flux.just(0, 0).delayElements(Duration.ofSeconds(5));
                            }
                        }
                )).reduce(Integer::sum).block();
        assertEquals(3, result.intValue());
    }

    @Test
    public void testTryLockFluxWithDurationByParallelThreads() {
        AtomicInteger lockSuccessCnt = new AtomicInteger(0);
        Flux.range(0, 3).flatMap(value -> this.defaultReactiveLockRegistry.getLock("testTryLockFluxWithDurationByParallelThreads")
                .lockFluxThenExecute(
                        createRepeatFunction(Duration.ofMillis(100), Duration.ofMillis(10)),
                        lockResult -> {
                            if (lockResult) {
                                lockSuccessCnt.getAndIncrement();
                            }
                            return Flux.empty();
                        })
        ).then().block();
        assertEquals(3, lockSuccessCnt.get());
    }

    private <T> Repeat<T> createRepeatFunction(Duration timeOut, Duration fixedBackoff) {
        return Repeat.<T>onlyIf(repeatContext -> true).timeout(timeOut).fixedBackoff(fixedBackoff);
    }

}
