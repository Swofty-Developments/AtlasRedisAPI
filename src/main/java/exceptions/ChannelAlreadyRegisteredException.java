package exceptions;

public class ChannelAlreadyRegisteredException extends RuntimeException  {

      public ChannelAlreadyRegisteredException(String errorMessage) {
            super(errorMessage);
      }

}