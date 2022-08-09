package net.swofty.redisapi.events;

import jdk.internal.event.Event;

public class RedisMessagingReceiveEvent extends Event {
    public String channel;
    public String message;
      RedisMessagingReceiveEvent (String channel, String message) {
            this.channel = channel;
            this.message = message;
      }
}
