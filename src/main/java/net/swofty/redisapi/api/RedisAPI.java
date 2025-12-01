package net.swofty.redisapi.api;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.swofty.redisapi.api.requests.DataStreamListener;
import net.swofty.redisapi.events.EventRegistry;
import net.swofty.redisapi.events.RedisMessagingReceiveEvent;
import net.swofty.redisapi.events.RedisMessagingReceiveInterface;
import net.swofty.redisapi.exceptions.CouldNotConnectToRedisException;
import lombok.Getter;
import lombok.Setter;
import net.swofty.redisapi.exceptions.ChannelAlreadyRegisteredException;
import net.swofty.redisapi.exceptions.MessageFailureException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedisAPI {
      private static final String REDIS_FULL_URI_PATTERN = "rediss?:\\/\\/(?:(?<user>\\w+)?:(?<password>[\\w-]+)@)?(?<host>[\\w.-]+):(?<port>\\d+)";
      private static final String REDIS_URI_PATTERN = "rediss?:\\/\\/[\\w.-]+:\\d+";
      private final ExecutorService executorService = Executors.newCachedThreadPool();

      @Getter
      private static RedisAPI instance = null;

      @Setter
      JedisPool pool;

      @Setter
      String filterId;

      /**
       * Creates a new main Redis pool instance, there will only ever be one at a time so #getInstance should be used after generation
       * @param credentials the credentials used to connect to the Redis server running, instanceof api.RedisCredentials
       * @exception CouldNotConnectToRedisException signifies either an issue with the password passed through or the URI that is passed through the method
       * @return main instance of the api.RedisAPI
       */
      public static RedisAPI generateInstance(@NonNull RedisCredentials credentials) {
            RedisAPI api = new RedisAPI();

            if (instance != null) {
                  instance.getPool().close();
            }

            String host = credentials.getHost();
            int port = credentials.getPort();

            String password = credentials.getPassword();
            String user = credentials.getUser();

            boolean ssl = credentials.isSsl();

            try {
                  JedisPool pool;

                  if (user != null) {
                        pool = new JedisPool(new JedisPoolConfig(), host, port, 2000, user, password, ssl);
                  } else if (password != null) {
                        pool = new JedisPool(new JedisPoolConfig(), host, port, 2000, password, ssl);
                  } else {
                        pool = new JedisPool(new JedisPoolConfig(), host, port, 2000, ssl);
                  }

                  api.setPool(pool);
            } catch (Exception e) {
                  throw new CouldNotConnectToRedisException("Either invalid Redis Credentials passed through; '" + credentials + "' OR invalid Redis Password passed through; '" + password + "'");
            }

            instance = api;
            return api;
      }

      /**
       * Creates a new main Redis pool instance, there will only ever be one at a time so #getInstance should be used after generation
       * @param uri the URI used to connect to the Redis server running (e.g. redis://username:password@localhost:6379 or redis://localhost:6379)
       * @exception CouldNotConnectToRedisException signifies either an issue with the password passed through or the URI that is passed through the method
       * @return main instance of the api.RedisAPI
       */
      public static RedisAPI generateInstance(@NonNull String uri) {
            String user = null, password = null, host, target;
            int port;

            Pattern pattern = java.util.regex.Pattern.compile(REDIS_FULL_URI_PATTERN);
            Matcher matcher = pattern.matcher(uri);

            if (matcher.matches()) {
                  user = matcher.group("user");
                  password = matcher.group("password");
                  host = matcher.group("host");
                  port = Integer.parseInt(matcher.group("port"));
            } else if (uri.matches(REDIS_URI_PATTERN)) {
                  target = uri.split("//")[1];
                  host = target.split(":")[0];
                  port = Integer.parseInt(target.split(":")[1]);
            } else {
                  throw new CouldNotConnectToRedisException("Invalid Redis URI passed through; '" + uri + "'");
            }

            boolean ssl = uri.startsWith("rediss");
            return generateInstance(new RedisCredentials(host, port, user, password, ssl));
      }

      /**
       * Creates a new main Redis pool instance, there will only ever be one at a time so #getInstance should be used after generation
       * @param uri the URI used to connect to the Redis server running (e.g. redis://localhost:6379)
       * @param password the password used to connect to the Redis server running
       * @exception CouldNotConnectToRedisException signifies either an issue with the password passed through or the URI that is passed through the method
       * @return main instance of the api.RedisAPI
       */
      public static RedisAPI generateInstance(@NonNull String uri, String password) {
            String user = null, host, target;
            int port;

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(REDIS_URI_PATTERN);
            java.util.regex.Matcher matcher = pattern.matcher(uri);

            if (matcher.matches()) {
                  host = matcher.group("host");
                  port = Integer.parseInt(matcher.group("port"));
            } else {
                  throw new CouldNotConnectToRedisException("Invalid Redis URI passed through; '" + uri + "'");
            }

            boolean ssl = uri.startsWith("rediss");

            return generateInstance(new RedisCredentials(host, port, user, password, ssl));
      }

      /**
       * Starts listeners for the Redis Pub/Sub channels
       */
      public void startListeners() {
            try {
                  registerChannel("internal-data-request", DataStreamListener.class);
            } catch (ChannelAlreadyRegisteredException ignored) {
                  System.out.println("[WARNING]: The internal data request channel has already been registered. This will cause issues if you are using the DataRequest API along with the Redis API." +
                          "\n Channel Name: internal-data-request");
            }

            new Thread(() -> {
                  try (Jedis jedis = getPool().getResource()) {
                        EventRegistry.pubSub = new JedisPubSub() {
                              @Override
                              public void onMessage(String channel, String message) {
                                    EventRegistry.handleAll(channel, message);
                              }
                        };
                        jedis.subscribe(EventRegistry.pubSub, ChannelRegistry.registeredChannels.stream().map((e) -> e.channelName).toArray(String[]::new));
                        getPool().returnResource(jedis);
                  } catch (Exception e) {
                        e.printStackTrace();
                  }
            }).start();
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
       * Asynchronously publishes a message to the generated instances redis pool
       * @param channel the channel object being published to, this is what should be registered on your other instances
       * @param message the message being sent across that channel
       * @return CompletableFuture<Void> representing the asynchronous operation
       */
      public CompletableFuture<Void> publishMessage(RedisChannel channel, String message) {
            return CompletableFuture.runAsync(() -> {
                  try (Jedis jedis = pool.getResource()) {
                        jedis.publish(channel.channelName, "none" + ";" + message);
                  } catch (Exception ex) {
                        throw new MessageFailureException("Failed to send message to redis", ex);
                  }
            }, executorService);
      }

      /**
       * Asynchronously publishes a message to the generated instances redis pool
       * @param filterId the filter id for the message being sent, this filter id is checked by all the receiving pools
       *                 to ensure that only a specific jedis pool handles the message
       * @param channel the channel object being published to, this is what should be registered on your other instances
       * @param message the message being sent across that channel
       * @return CompletableFuture<Void> representing the asynchronous operation
       */
      public CompletableFuture<Void> publishMessage(String filterId, RedisChannel channel, String message) {
            return CompletableFuture.runAsync(() -> {
                  try (Jedis jedis = pool.getResource()) {
                        jedis.publish(channel.channelName, filterId + ";" + message);
                  } catch (Exception ex) {
                        throw new MessageFailureException("Failed to send message to redis", ex);
                  }
            }, executorService);
      }

      /**
       * Used to register a redis channel, this must be done before sending any messages on this channel
       * @param channelName the name of the channel, this is what is used when publishing a message
       * @param receiveEventClass the class which extends RedisMessagingReceiveInterface, this is where incoming messages on this
       *                          channel will be sent
       * @return object of the registered RedisChannel
       * @throws ChannelAlreadyRegisteredException exception is thrown if channel with same name is already registered
       */
      public RedisChannel registerChannel(String channelName, @NonNull Class<? extends RedisMessagingReceiveInterface> receiveEventClass) {
            RedisChannel channel = new RedisChannel(channelName, receiveEventClass);
            ChannelRegistry.registerChannel(channel);
            return channel;
      }

      /**
       * Used to register a redis channel, this must be done before sending any messages on this channel
       * @param channelName the name of the channel, this is what is used when publishing a message
       * @param receiveEventConsumer the consumer which has RedisMessagingReceiveEvent, this is where incoming messages on this
       *                          channel will be sent
       * @return object of the registered RedisChannel
       * @throws ChannelAlreadyRegisteredException exception is thrown if channel with same name is already registered
       */
      public RedisChannel registerChannel(String channelName, @NonNull Consumer<RedisMessagingReceiveEvent> receiveEventConsumer) {
            RedisChannel channel = new RedisChannel(channelName, receiveEventConsumer);
            ChannelRegistry.registerChannel(channel);
            return channel;
      }
}