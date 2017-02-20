package org.cstamas.vertx.bitsy.rom.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface Query
{
  void query(final JsonObject params, Handler<AsyncResult<JsonObject>> handler);
}
