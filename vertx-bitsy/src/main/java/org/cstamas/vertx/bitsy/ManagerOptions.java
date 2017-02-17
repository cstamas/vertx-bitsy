package org.cstamas.vertx.bitsy;

import javax.annotation.Nullable;

import io.vertx.core.json.JsonObject;

import static java.util.Objects.requireNonNull;

public class ManagerOptions
{
  private final String home;

  public ManagerOptions(final String home) {
    this.home = requireNonNull(home);
  }

  public String getHome() {
    return home;
  }

  public static ManagerOptions fromJsonObject(@Nullable final JsonObject config) {
    String home = "target/bitsy";
    if (config != null) {
      home = config.getString("home", home);
    }
    return new ManagerOptions(home);
  }
}
