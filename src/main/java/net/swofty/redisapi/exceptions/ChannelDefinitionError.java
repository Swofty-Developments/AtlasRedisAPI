package net.swofty.redisapi.exceptions;

/**
 * This class is a custom exception that is thrown when a channel definition is invalid, this is usually because
 * there is no event consumer or class passed through
 */
public class ChannelDefinitionError extends RuntimeException {

    public ChannelDefinitionError(String errorMessage) {
        super(errorMessage);
    }

}