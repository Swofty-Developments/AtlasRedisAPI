package net.swofty.redisapi.exceptions;

public class MessageFailureException extends RuntimeException {
    public MessageFailureException(String errorMessage, Throwable t) {
        super(errorMessage);
        this.setStackTrace(t.getStackTrace());
    }
}
