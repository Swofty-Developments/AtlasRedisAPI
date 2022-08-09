package net.swofty.redisapi.api;

import net.swofty.redisapi.events.RedisMessagingReceiveEvent;
import net.swofty.redisapi.events.RedisMessagingReceiveInterface;

import java.util.function.Consumer;

public class RedisChannel {

      /**
       * Class that contains the function ran when a message is received on this channel
       */
      public Consumer<RedisMessagingReceiveEvent> receiveEvent;
      /**
       * Class that contains the function ran when a message is received on this channel
       */
      public Class<? extends RedisMessagingReceiveInterface> receiveInterface;

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
      }

      public RedisChannel(String channelName, Class<? extends RedisMessagingReceiveInterface> classz) {
            this.channelName = channelName;
            this.receiveInterface = classz;
      }

}
