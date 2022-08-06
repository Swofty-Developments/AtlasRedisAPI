package exceptions;

public class CouldNotConnectToRedisException extends RuntimeException  {

      public CouldNotConnectToRedisException(String errorMessage) {
            super(errorMessage);
      }

}