package org.cstamas.vertx.bitsy.rom.service;

import java.util.Map;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface RomDatabase
{
  String ADDRESS = RomDatabase.class.getName() + "-rom";

  @Fluent
  RomDatabase status(Handler<AsyncResult<JsonObject>> handler);

  @Fluent
  RomDatabase query(String queryName,
                    JsonObject params,
                    Handler<AsyncResult<JsonObject>> handler);

  @Fluent
  RomDatabase gremlin(Map<String, String> params,
                      String script,
                      Handler<AsyncResult<JsonObject>> handler);
}
