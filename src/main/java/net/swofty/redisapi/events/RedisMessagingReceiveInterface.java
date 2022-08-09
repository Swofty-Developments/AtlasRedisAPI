package net.swofty.redisapi.events;

public interface RedisMessagingReceiveInterface {
    void onMessage(String channel, String message);
}
