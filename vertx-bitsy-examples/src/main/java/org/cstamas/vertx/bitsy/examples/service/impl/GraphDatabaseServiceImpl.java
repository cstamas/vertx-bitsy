package org.cstamas.vertx.bitsy.examples.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;

import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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
                                            final Handler<AsyncResult<List<String>>> handler)
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
        try {
          String string = String.valueOf(scriptEngine.eval(script, bindings));
          handler.handle(Future.succeededFuture(Collections.singletonList(string)));
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
