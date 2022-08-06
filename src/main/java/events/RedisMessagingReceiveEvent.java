package events;

public interface RedisMessagingReceiveEvent {
      void onMessage(String channel, String message);
}
