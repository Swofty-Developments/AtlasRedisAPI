package net.swofty.redisapi.exceptions;

/**
 * This class is a custom exception class that is thrown when a class is not registered yet there is a message
 * trying to be sent through it
 */
public class ChannelNotRegisteredException extends RuntimeException {

    public ChannelNotRegisteredException(String errorMessage) {
        super(errorMessage);
    }

}