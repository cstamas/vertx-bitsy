package org.cstamas.vertx.bitsy.rom.service.support;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;

import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.cstamas.vertx.bitsy.Database;
import org.cstamas.vertx.bitsy.rom.service.Query;
import org.cstamas.vertx.bitsy.rom.service.QueryNotFoundException;
import org.cstamas.vertx.bitsy.rom.service.RomDatabase;

import static java.util.Objects.requireNonNull;

public class RomDatabaseImpl
    implements RomDatabase
{
  private static final Logger log = LoggerFactory.getLogger(RomDatabaseImpl.class);

  private final String nodeId;

  private final Database database;

  private final Map<String, Query> queries;

  public RomDatabaseImpl(final String nodeId, final Database database, final Map<String, Query> queries) {
    this.nodeId = requireNonNull(nodeId);
    this.database = requireNonNull(database);
    this.queries = requireNonNull(queries);
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
  public RomDatabase query(final String queryName,
                           final JsonObject params,
                           final Handler<AsyncResult<JsonObject>> handler)
  {
    database.readTx(g -> {
      if (g.failed()) {
        log.error("Could not obtain graph", g.cause());
        handler.handle(Future.failedFuture(g.cause()));
      }
      else {
        Query query = queries.get(queryName);
        if (query == null) {
          handler.handle(Future.failedFuture(new QueryNotFoundException()));
        }
        else {
          query.query(params, handler);
        }
      }
    });
    return this;
  }

  @Override
  public RomDatabase gremlin(final Map<String, String> params,
                             final String script,
                             final Handler<AsyncResult<JsonObject>> handler)
  {
    database.readTx(g -> {
      if (g.failed()) {
        log.error("Could not obtain graph", g.cause());
        handler.handle(Future.failedFuture(g.cause()));
      }
      else {
        GremlinGroovyScriptEngine scriptEngine = new GremlinGroovyScriptEngine();
        Bindings bindings = scriptEngine.createBindings();
        bindings.put("g", g.result());
        bindings.putAll(params);
        Object result = null;
        try {
          result = scriptEngine.eval(script, bindings);
          HashMap<String, Object> resultMap = new HashMap<>();
          resultMap.put("result", result);
          JsonObject json = new JsonObject(Json.encode(resultMap));
          handler.handle(Future.succeededFuture(json));
        }
        catch (EncodeException e) {
          log.error("Result encode exception: result={}", result, e);
          handler.handle(Future.failedFuture(e));
        }
        catch (Exception e) {
          log.error("Script execution error: params={}, script={}", params, script, e);
          handler.handle(Future.failedFuture(e));
        }
      }
    });
    return this;
  }
}
