package net.swofty.redisapi.api.requests;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DataRequestResponder {
    public static final Map<String, DataRequestResponder> RESPONDERS = new HashMap<>();

    private final Function<JSONObject, JSONObject> callback;

    protected DataRequestResponder(Function<JSONObject, JSONObject> callback) {
        this.callback = callback;
    }

    public JSONObject respond(JSONObject request) {
        return this.callback.apply(request);
    }

    /**
     * Creates a new Data Request Responder. Must be registered before {@link net.swofty.redisapi.api.RedisAPI#startListeners()} in order to work properly.
     * @param key The key to respond to.
     * @param callback Callback, has a JSONObject parameter request, and returns a JSONObject response, request and response both can be empty.
     * @return The created DataRequestResponder, not entirely useful, but still there.
     */
    public static DataRequestResponder create(String key, Function<JSONObject, JSONObject> callback) {
        DataRequestResponder responder = new DataRequestResponder(callback);
        RESPONDERS.put(key, responder);
        return responder;
    }

    /**
     * Get a DataRequestResponder by key.
     * @param key The key to get the DataRequestResponder by.
     * @return The DataRequestResponder, or null if it doesn't exist.
     */
    public static DataRequestResponder get(String key) {
        return RESPONDERS.get(key);
    }
}
