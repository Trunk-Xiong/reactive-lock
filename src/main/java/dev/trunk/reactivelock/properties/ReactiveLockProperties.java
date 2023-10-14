package dev.trunk.reactivelock.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

/**
 * {@link ReactiveLockProperties} defines properties used for setting up reactive lock
 * @author Trunk Xiong
 * @since 1.0
 */
@Getter
@Setter
public class ReactiveLockProperties {
    /**
     * Max idle duration of a reactive lock, which defaults to 60 seconds. A lock will be evicted if its has not been used at all during specified duration
     */
    @Value("${reactive.lock.max.idle.duration:1m}")
    private Duration maxIdleDuration;

    /**
     * To enable reactive lock or not
     */
    @Value("${reactive.lock.enforced:false}")
    private boolean enforced;

    /**
     * Strips on reactive lock
     */
    @Value("${reactive.lock.strips:100}")
    private int lockStrips;
}
