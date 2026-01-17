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
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
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
      private static final String REDIS_FULL_URI_PATTERN = "rediss?://(?:(?<user>\\w+)?:(?<password>[\\w-]+)@)?(?<host>[\\w.-]+):(?<port>\\d+)";
      private static final String REDIS_URI_PATTERN = "rediss?://[\\w.-]+:\\d+";
      private static final Duration DEFAULT_REDIS_TIMEOUT = Duration.ofSeconds(2);

      private final ExecutorService executorService = Executors.newCachedThreadPool();

      @Getter
      private static RedisAPI instance = null;

      @Setter
      RedisClient pool;

      String filterId;

      // Store params so we can create a dedicated non-pooled connection
      transient volatile HostAndPort hostAndPort;
      transient volatile DefaultJedisClientConfig clientConfig;

      transient volatile Jedis subscriberJedis;
      transient volatile Thread subscriberThread;

      /**
       * Creates a new main Redis pool instance, there will only ever be one at a time so #getInstance should be used after generation
       * @param credentials the credentials used to connect to the Redis server running, instanceof api.RedisCredentials
       * @exception CouldNotConnectToRedisException signifies either an issue with the password passed through or the URI that is passed through the method
       * @return main instance of the api.RedisAPI
       */
      public static RedisAPI generateInstance(@NonNull RedisCredentials credentials) {
            RedisAPI api = new RedisAPI();

            if (instance != null) {
                  try {
                        instance.shutdown();
                  } catch (Exception ignored) {
                  }
            }

            String host = credentials.host();
            int port = credentials.port();

            String password = credentials.password();
            String user = credentials.user();

            boolean ssl = credentials.ssl();

            try {
                  DefaultJedisClientConfig.Builder clientConfigBuilder = DefaultJedisClientConfig.builder()
                        .timeoutMillis((int) DEFAULT_REDIS_TIMEOUT.toMillis())
                        .blockingSocketTimeoutMillis((int) DEFAULT_REDIS_TIMEOUT.toMillis())
                        .ssl(ssl);

                  if (user != null && password != null) {
                        clientConfigBuilder.user(user).password(password);
                  } else if (password != null) {
                        clientConfigBuilder.password(password);
                  }

                  HostAndPort hap = new HostAndPort(host, port);
                  DefaultJedisClientConfig cfg = clientConfigBuilder.build();

                  RedisClient client = RedisClient.builder()
                        .hostAndPort(hap)
                        .clientConfig(cfg)
                        .build();

                  api.hostAndPort = hap;
                  api.clientConfig = cfg;
                  api.setPool(client);
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
       * Starts listeners for the Redis Pub/Sub channels.
       * This creates a single long-lived subscriber connection.
       */
      public void startListeners() {
            try {
                  registerChannel("internal-data-request", DataStreamListener.class);
            } catch (ChannelAlreadyRegisteredException ignored) {
                  System.out.println("[WARNING]: The internal data request channel has already been registered. This will cause issues if you are using the DataRequest API along with the Redis API." +
                          "\n Channel Name: internal-data-request");
            }

            // Don't start multiple subscriber threads.
            if (subscriberThread != null && subscriberThread.isAlive()) return;

            subscriberThread = new Thread(() -> {
                  Jedis jedis = null;
                  try {
                        // Dedicated standalone connection for Pub/Sub.
                        jedis = new Jedis(hostAndPort, clientConfig);
                        subscriberJedis = jedis;

                        EventRegistry.pubSub = new JedisPubSub() {
                              @Override
                              public void onMessage(String channel, String message) {
                                    EventRegistry.handleAll(channel, message);
                              }
                        };

                        String[] channels = ChannelRegistry.registeredChannels.stream()
                              .map((e) -> e.channelName)
                              .toArray(String[]::new);
                        jedis.subscribe(EventRegistry.pubSub, channels);
                  } catch (Exception e) {
                        e.printStackTrace();
                  } finally {
                        try {
                              if (jedis != null) jedis.close();
                        } catch (Exception ignored) {
                        }
                        subscriberJedis = null;
                  }
            }, "AtlasRedisAPI-Subscriber");

            subscriberThread.setDaemon(true);
            subscriberThread.start();
      }

      /**
       * Stops the Pub/Sub listener thread (if running), closes the pool, and shuts down executors.
       */
      public void shutdown() {
            try {
                  if (EventRegistry.pubSub != null) {
                        EventRegistry.pubSub.unsubscribe();
                  }
            } catch (Exception ignored) {
            }

            try {
                  if (subscriberJedis != null) {
                        subscriberJedis.close();
                  }
            } catch (Exception ignored) {
            }

            try {
                  if (pool != null) {
                        pool.close();
                  }
            } catch (Exception ignored) {
            }

            executorService.shutdown();
      }

    /**
       * Asynchronously publishes a message to the generated instances redis pool
       * @param channel the channel object being published to, this is what should be registered on your other instances
       * @param message the message being sent across that channel
       * @return CompletableFuture<Void> representing the asynchronous operation
       */
      public CompletableFuture<Void> publishMessage(RedisChannel channel, String message) {
            return CompletableFuture.runAsync(() -> {
                  try {
                        pool.publish(channel.channelName, "none" + ";" + message);
                  } catch (Exception ex) {
                        throw new MessageFailureException("Failed to send message to redis", ex);
                  }
            }, executorService);
      }

      /**
       * Asynchronously publishes a message to the generated instances redis pool
       * @param filterId the filter id for the message being sent, this filter id is checked by all the receiving pools
       *                 to ensure that only a specific Jedis pool handles the message
       * @param channel the channel object being published to, this is what should be registered on your other instances
       * @param message the message being sent across that channel
       * @return CompletableFuture<Void> representing the asynchronous operation
       */
      public CompletableFuture<Void> publishMessage(String filterId, RedisChannel channel, String message) {
            return CompletableFuture.runAsync(() -> {
                  try {
                        pool.publish(channel.channelName, filterId + ";" + message);
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