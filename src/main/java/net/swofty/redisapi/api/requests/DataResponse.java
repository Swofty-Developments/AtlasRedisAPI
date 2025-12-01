package net.swofty.redisapi.api.requests;

import org.json.JSONObject;

/**
 * The response to a DataRequest.
 * @param data The data object, will be null if the request has timed out.
 * @param latency The latency of the request, normal range is between 1-10ms, unless the server is under heavy load or the Redis server is running on a different machine.
 */
public record DataResponse(JSONObject data, long latency) {
}
