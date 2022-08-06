package net.swofty.redisapi.api;

import net.swofty.redisapi.events.RedisMessagingReceiveEvent;

public class RedisChannel {

      /**
       * Class that contains the function ran when a message is received on this channel
       */
      public Class<? extends RedisMessagingReceiveEvent> receiveEvent;

      /**
       * Name of the channel that messages are sent through
       */
      public String channelName;

      /**
       * Timestamp in unix milliseconds of when the channel last had a message received through it
       */
      public Long timestamp;

      public RedisChannel(String channelName, Class<? extends RedisMessagingReceiveEvent> receiveEventClass) {
            this.channelName = channelName;
            this.receiveEvent = receiveEventClass;
      }

}
