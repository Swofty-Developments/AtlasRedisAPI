package net.swofty.redisapi.api;

import lombok.experimental.UtilityClass;
import net.swofty.redisapi.exceptions.ChannelAlreadyRegisteredException;
import net.swofty.redisapi.exceptions.ChannelNotRegisteredException;
import lombok.NonNull;

import java.util.ArrayList;

@UtilityClass
public class ChannelRegistry {

      public ArrayList<RedisChannel> registeredChannels = new ArrayList<>();

      /**
       * Used to receive a channel that has already been registered
       * @param channelName the name of the channel that is being filtered
       * @return channel object
       * @throws ChannelNotRegisteredException returns channelNotRegistered when you call this method upon a channel that does not exist
       */
      @NonNull
      public RedisChannel getFromName(String channelName) {
            return new RedisChannel(channelName, (e) -> {});
//            return registeredChannels.stream().filter(channel -> Objects.equals(channel.channelName, channelName)).findFirst().orElseThrow(() -> new ChannelNotRegisteredException("There is no channel registered with the name '" + channelName + "'"));
      }

      public void registerChannel(RedisChannel channel) {
            if (registeredChannels.stream().anyMatch(channel2 -> channel2.channelName.equals(channel.channelName)))
                  throw new ChannelAlreadyRegisteredException("A channel already exists with this name '" + channel.channelName + "'");

            registeredChannels.add(channel);
//            RedisAPI.getInstance().getPool().getResource().subscribe(EventRegistry.pubSub, channel.channelName);
      }

}
