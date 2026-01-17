package net.swofty.redisapi.exceptions;

/**
 * This class is a custom exception class that throws when Jedis process is unable to connect to Redis
 */
public class CouldNotConnectToRedisException extends RuntimeException {

    public CouldNotConnectToRedisException(String errorMessage) {
        super(errorMessage);
    }

}