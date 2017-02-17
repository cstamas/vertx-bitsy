package org.cstamas.vertx.bitsy.rom.service.impl;

import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.cstamas.vertx.bitsy.Database;
import org.cstamas.vertx.bitsy.rom.service.RomDatabase;

import static java.util.Objects.requireNonNull;

public class RomDatabaseImpl
    implements RomDatabase
{
  private final String nodeId;

  private final Database database;

  public RomDatabaseImpl(final String nodeId, final Database database) {
    this.nodeId = requireNonNull(nodeId);
    this.database = requireNonNull(database);
  }

  @Override
  public RomDatabase status(final Handler<AsyncResult<JsonObject>> handler) {
    JsonObject json = new JsonObject();
    json.put("status", "ok");
    json.put("nodeId", nodeId);
    handler.handle(Future.succeededFuture(json));
    return this;
  }

  @Override
  public RomDatabase query(final Map<String, String> params,
                           final String queryName,
                           final Handler<AsyncResult<JsonObject>> handler)
  {
    return this;
  }

  @Override
  public RomDatabase gremlin(final Map<String, String> params,
                             final String script,
                             final Handler<AsyncResult<JsonObject>> handler)
  {
    return this;
  }
}
