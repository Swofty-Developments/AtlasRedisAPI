package net.swofty.redisapi.api.requests;

import net.swofty.redisapi.api.ChannelRegistry;
import net.swofty.redisapi.api.RedisAPI;
import net.swofty.redisapi.util.RedisParsableMessage;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataRequest {
    public static final Map<String, JSONObject> RECEIVED_DATA = new HashMap<>();

    private final String id;
    private final String filter;
    private final String key;
    private final JSONObject data;

    /**
     * Create a new data request to get a specific object of data from a specific filter ID.
     * @param filterID The filter ID where you want to receive data from, can be "all" for all listeners.
     * @param key The data identifier key.
     */
    public DataRequest(String filterID, String key, JSONObject data) {
        this.id = UUID.randomUUID().toString();
        this.filter = filterID;
        this.key = key;
        this.data = data;
    }

    public CompletableFuture<DataResponse> await() {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            JSONObject request = new JSONObject();
            request.put("id", id);
            request.put("key", key);
            request.put("data", data);
            request.put("sender", "proxy");
            request.put("stream", StreamType.REQUEST.name());

            RedisAPI.getInstance().publishMessage(filter, ChannelRegistry.getFromName("internal-data-request"), RedisParsableMessage.from(request).formatForSend());

            int timeout = 0;
            while (!RECEIVED_DATA.containsKey(id)) {
                try { Thread.sleep(1); timeout++; } catch (InterruptedException ignored) { }
                if (timeout >= 100) break;
            }

            JSONObject response = RECEIVED_DATA.get(id);
            RECEIVED_DATA.remove(id);
            long latency = (System.currentTimeMillis() - start);
            return new DataResponse(response, latency);
        });
    }

    public enum StreamType {
        REQUEST,
        RESPONSE
    }
}
