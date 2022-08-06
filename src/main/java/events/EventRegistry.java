package events;

import api.ChannelRegistry;
import api.RedisAPI;
import api.RedisChannel;
import lombok.SneakyThrows;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.Optional;

public class EventRegistry {

      public static JedisPubSub pubSub = null;

      @SneakyThrows
      public static void handleAll(String channel, String message) {
            String filterID = message.split(";")[0];

            if (filterID.equals("all") || filterID.equals(RedisAPI.getInstance().getFilterId())) {
                  Optional<RedisChannel> optionalChannelBeingCalled = ChannelRegistry.registeredChannels.stream().filter(channel2 -> Objects.equals(channel2.channelName, channel)).findAny();
                  if (optionalChannelBeingCalled.isPresent()) {
                        RedisChannel channelBeingCalled = optionalChannelBeingCalled.get();
                        RedisMessagingReceiveEvent event = channelBeingCalled.receiveEvent.newInstance();
                        event.onMessage(channel, message.replace(filterID + ";", ""));
                  }
            }
      }

}
