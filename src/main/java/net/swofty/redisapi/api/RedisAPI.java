package net.swofty.redisapi.api;

import net.swofty.redisapi.events.EventRegistry;
import net.swofty.redisapi.events.RedisMessagingReceiveEvent;
import net.swofty.redisapi.exceptions.CouldNotConnectToRedisException;
import lombok.Getter;
import lombok.Setter;
import net.swofty.redisapi.exceptions.ChannelAlreadyRegisteredException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class RedisAPI {

      @Getter
      public static RedisAPI instance = null;
      @Getter
      @Setter
      private JedisPool pool;
      @Getter
      @Setter
      private String filterId;

      /**
       * Creates a new main Redis pool instance, there will only ever be one at a time so #getInstance should be used after generation
       * @param uri the URI used to connect to the Redis server running
       * @param password the password used to connect to the Redis server running
       * @exception CouldNotConnectToRedisException signifies either an issue with the password passed through or the URI that is passed through the method
       * @return main instance of the api.RedisAPI
       */
      public static RedisAPI generateInstance(String uri, String password) {
            RedisAPI api = new RedisAPI();

            if (instance != null) {
                  instance.getPool().close();
            }

            String host = uri.split("//")[1].split(":")[0];
            int port = Integer.parseInt(uri.split("//")[1].split(":")[1]);

            try {
                  if (password == null) {
                        api.setPool(new JedisPool(new JedisPoolConfig(), host, port, 20));
                  } else {
                        api.setPool(new JedisPool(new JedisPoolConfig(), host, port, 20, password));
                  }
            } catch (Exception e) {
                  throw new CouldNotConnectToRedisException("Either invalid Redis URI passed through; '" + uri + "' OR invalid Redis Password passed through; '" + password + "'");
            }

            Jedis jedis = api.getPool().getResource();
            jedis.connect();

            EventRegistry.pubSub = new JedisPubSub() {
                  @Override
                  public void onMessage(String channel, String message) {
                        System.out.println("Received " + message);
                        EventRegistry.handleAll(channel, message);
                        super.onMessage(channel, message);
                  }
            };

            instance = api;
            return api;
      }

      /**
       * Creates a new main Redis pool instance, there will only ever be one at a time so #getInstance should be used after generation
       * @param uri the URI used to connect to the Redis server running
       * @exception CouldNotConnectToRedisException signifies either an issue with the password passed through or the URI that is passed through the method
       * @return main instance of the api.RedisAPI
       */
      public static RedisAPI generateInstance(String uri) {
            return generateInstance(uri, null);
      }

      /**
       * This method is used to set a filter ID onto your Redis Pool, you can then use this when publishing messages to
       * a channel to ensure that a specific RedisAPI instance receives your message
       * @param filterId the filter id that you want to set your redis pool to
       */
      public void setFilterID(String filterId) {
            this.filterId = filterId;
      }

      /**
       * Publishes a message to the generated instances redis pool
       * @param channel the channel object being published to, this is what should be registered on your other instances
       * @param message the message being sent across that channel
       */
      public void publishMessage(RedisChannel channel, String message) {
            Jedis jedis = pool.getResource();
            jedis.connect();
            jedis.publish(channel.channelName, "all" + ";" + message);
      }

      /**
       *  Publishes a message to the generated instances redis pool
       * @param filterId the filter id for the message being sent, this filter id is checked by all the receiving pools
       *                 to ensure that only a specific jedis pool handles the message
       * @param channel the channel object being published to, this is what should be registered on your other instances
       * @param message the message being sent across that channel
       */
      public void publishMessage(String filterId, RedisChannel channel, String message) {
            Jedis jedis = pool.getResource();
            jedis.connect();
            jedis.publish(channel.channelName, filterId + ";" + message);
      }

      /**
       * Used to register a redis channel, this must be done before sending any messages on this channel
       * @param channelName the name of the channel, this is what is used when publishing a message
       * @param receiveEventClass the class which extends RedisMessagingReceiveEvent, this is where incoming messages on this
       *                          channel will be sent
       * @return object of the registered RedisChannel
       * @throws ChannelAlreadyRegisteredException exception is thrown if channel with same name is already registered
       */
      public RedisChannel registerChannel(String channelName, Class<? extends RedisMessagingReceiveEvent> receiveEventClass) {
            RedisChannel channel = new RedisChannel(channelName, receiveEventClass);
            ChannelRegistry.registerChannel(channel);
            return channel;
      }
}
