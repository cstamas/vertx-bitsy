package org.cstamas.vertx.bitsy;

import static java.util.Objects.requireNonNull;

public class ConnectionOptions
{
  private final String name;

  public ConnectionOptions(final String name)
  {
    this.name = requireNonNull(name);
  }

  public String name() {
    return name;
  }

  public static class Builder
  {
    private final String name;

    public Builder(final String name) {
      this.name = requireNonNull(name);
    }

    public ConnectionOptions build() {
      return new ConnectionOptions(name);
    }
  }
}
