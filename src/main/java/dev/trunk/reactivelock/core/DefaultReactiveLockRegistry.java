package dev.trunk.reactivelock.core;

/**This provides functionalities to manipulate {@link DefaultReactiveLock}
 * @author Trunk Xiong
 * @since 1.0
 */
public class DefaultReactiveLockRegistry extends AbstractAutoCleanupReactiveLockRegistry {
    public DefaultReactiveLockRegistry(int lockStrips) {
        super(lockStrips);
    }

    @Override
    protected ReactiveLock newReactiveLock(Object lockKey) {
        return new DefaultReactiveLock();
    }
}
