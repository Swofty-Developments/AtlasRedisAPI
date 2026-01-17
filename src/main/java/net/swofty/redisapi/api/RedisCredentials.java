package net.swofty.redisapi.api;

import lombok.NonNull;

public record RedisCredentials(@NonNull String host, int port, String user, String password, boolean ssl) {

    public RedisCredentials(@NonNull String host, int port, @NonNull String password, boolean ssl) {
        this(host, port, null, password, ssl);
    }

}
