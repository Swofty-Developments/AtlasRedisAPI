package net.swofty.redisapi.api;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RedisCredentials {

      @NonNull
      String host;

      @NonNull
      int port;

      String user;
      String password;

      boolean ssl;

      public RedisCredentials(@NonNull String host, @NonNull int port, @NonNull String password, boolean ssl) {
            this.host = host;
            this.port = port;
            this.password = password;
            this.user = null;
            this.ssl = ssl;
      }

      @Override
      public String toString() {
            return "RedisCredentials(" +
                  "host=" + this.getHost() + ", " +
                  "port=" + this.getPort() + ", " +
                  "user=" + this.getUser() + ", " +
                  "password=" + this.getPassword() +
            ")";
      }

}
