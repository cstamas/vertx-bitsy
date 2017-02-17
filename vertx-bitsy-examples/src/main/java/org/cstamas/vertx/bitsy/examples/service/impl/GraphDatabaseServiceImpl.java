package org.cstamas.vertx.bitsy.examples.service.impl;

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
import org.cstamas.vertx.bitsy.Database;
import org.cstamas.vertx.bitsy.examples.service.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Default implementation.
 */
public class GraphDatabaseServiceImpl
    implements GraphDatabaseService
{
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final Database database;

  public GraphDatabaseServiceImpl(final Database database) {
    this.database = requireNonNull(database);
    new GremlinGroovyScriptEngine(); // warm up to avoid delay on 1st request
  }

  @Override
  public GraphDatabaseService gremlinScript(final Map<String, String> params,
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
