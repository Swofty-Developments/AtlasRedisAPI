package api;

import events.EventRegistry;
import exceptions.ChannelAlreadyRegisteredException;
import exceptions.ChannelNotRegisteredException;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Objects;

public class ChannelRegistry {

      public static ArrayList<RedisChannel> registeredChannels = new ArrayList<>();

      /**
       * Used to receive a channel that has already been registered
       * @param channelName the name of the channel that is being filtered
       * @return channel object
       * @throws ChannelNotRegisteredException returns channelNotRegistered when you call this method upon a channel that does not exist
       */
      @NonNull
      public static RedisChannel getFromName(String channelName) {
            return registeredChannels.stream().filter(channel -> Objects.equals(channel.channelName, channelName)).findFirst().orElseThrow(() -> new ChannelNotRegisteredException("There is no channel registered with the name '" + channelName + "'"));
      }

      public static void registerChannel(RedisChannel channel) {
            if (registeredChannels.stream().anyMatch(channel2 -> channel2.channelName.equals(channel.channelName)))
                  throw new ChannelAlreadyRegisteredException("A channel already exists with this name '" + channel.channelName + "'");

            registeredChannels.add(channel);
            Utility.runAsync(() -> RedisAPI.getInstance().getPool().getResource().subscribe(EventRegistry.pubSub, channel.channelName));
      }

}
