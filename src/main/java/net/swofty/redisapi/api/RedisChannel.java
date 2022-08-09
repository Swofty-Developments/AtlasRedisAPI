package net.swofty.redisapi.api;

import net.swofty.redisapi.events.RedisMessagingReceiveEvent;
import net.swofty.redisapi.events.RedisMessagingReceiveInterface;

import java.util.function.Consumer;

public class RedisChannel {

      /**
       * Consumer that contains the function ran when a message is received on this channel
       */
      public Consumer<RedisMessagingReceiveEvent> receiveEvent;
      /**
       * Class that contains the function ran when a message is received on this channel
       */
      public Class<? extends RedisMessagingReceiveInterface> receiveInterface;

      /**
       * Used to determine what type of function is being used to receive messages.
       */
      public ChannelFunctionType functionType;

      /**
       * Name of the channel that messages are sent through
       */
      public String channelName;

      /**
       * Timestamp in unix milliseconds of when the channel last had a message received through it
       */
      public Long timestamp;

      public RedisChannel(String channelName, Consumer<RedisMessagingReceiveEvent> receiveEventClass) {
            this.channelName = channelName;
            this.receiveEvent = receiveEventClass;
            this.functionType = ChannelFunctionType.CONSUMER;
      }

      public RedisChannel(String channelName, Class<? extends RedisMessagingReceiveInterface> classz) {
            this.channelName = channelName;
            this.receiveInterface = classz;
            this.functionType = ChannelFunctionType.CLASS;
      }

      /**
       * This function returns the timestamp of the last time the channel had something sent through it
       *
       * @return The timestamp of the last time the user was updated.
       */
      public java.lang.Long getTimestamp() {
            return timestamp;
      }
}
