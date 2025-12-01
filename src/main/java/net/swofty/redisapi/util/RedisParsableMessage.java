package net.swofty.redisapi.util;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This is a utility class, used for sending JSONObjects over Redis instead of working with raw Strings.
 */
@Getter
public class RedisParsableMessage {
    private final JSONObject json;

    protected RedisParsableMessage(JSONObject json) {
        this.json = json;
    }

    /**
     * Builds a new RedisParsableMessage from a JSONObject.
     * @param fields The fields to build the JSONObject from.
     * @return The built RedisParsableMessage.
     */
    public static RedisParsableMessage from( Map<String, Object> fields) {
        return from(new JSONObject(fields));
    }

    /**
     * Builds a new RedisParsableMessage from a JSONObject.
     * @param obj The JSONObject to build the RedisParsableMessage from.
     * @return The built RedisParsableMessage.
     */
    public static RedisParsableMessage from(JSONObject obj) {
        return new RedisParsableMessage(obj);
    }

    /**
     * Parse a RedisParsableMessage from a raw String.
     * @param raw The raw String to parse.
     * @return The parsed RedisParsableMessage.
     * @throws IllegalArgumentException if the raw String is not a valid JSONObject.
     */
    public static RedisParsableMessage parse(String raw) {
        String toParse = raw;
        if (raw.contains(";")) {
            String[] split = raw.split(";");
            toParse = split[1];
        }
        return new RedisParsableMessage(new JSONObject(toParse));
    }

    /**
     * Formats the JSONObject into a String to send over Redis, this is the same as {@link #json#toString()}.
     * @return The formatted String.
     */
    public String formatForSend() {
        return json.toString();
    }

    @Override
    public String toString() {
        return formatForSend();
    }

    /**
     * Get an object from the JSONObject.
     * @param key The key to get the object from.
     * @param defaultValue The default value to return if the key is not found.
     * @return The object.
     * @param <T> The type of the object.
     */
    public <T> T get(String key, T defaultValue) {
        return json.has(key) ? (T) json.get(key) : defaultValue;
    }

    /*
     * Beyond here are some utility methods for getting data from the JSONObject.
     */

    /**
     * Get a UUID from the JSONObject.
     * @param key The key to get the UUID from.
     * @return The UUID.
     */
    public UUID getUUID(String key) {
        return UUID.fromString(get(key, ""));
    }

    public JSONArray getJsonArray(String key) {
        return json.has(key) ? json.getJSONArray(key) : new JSONArray();
    }

    public List<String> getStringList(String key) {
        return json.has(key) ? json.getJSONArray(key).toList().stream().map(String::valueOf).toList() : new ArrayList<>();
    }

    public boolean getBoolean(String key) {
        return json.has(key) && json.getBoolean(key);
    }
}
