package net.swofty.redisapi.exceptions;

import java.util.function.Supplier;

public class ChannelNotRegisteredException extends RuntimeException {

      public ChannelNotRegisteredException(String errorMessage) {
            super(errorMessage);
      }

}