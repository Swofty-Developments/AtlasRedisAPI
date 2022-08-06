package net.swofty.redisapi.exceptions;

public class CouldNotConnectToRedisException extends RuntimeException  {

      public CouldNotConnectToRedisException(String errorMessage) {
            super(errorMessage);
      }

}