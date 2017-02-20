package org.cstamas.vertx.bitsy.examples;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.cstamas.vertx.bitsy.Database;
import org.cstamas.vertx.bitsy.rom.service.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Simple "follow" query example.
 * <p/>
 * Params:
 * <pre>{@code
 * {
 *   "start": { "key", "propertyName", "value": "propertyValue" },
 *   "direction": "OUT",
 *   "label": "edgeLabelToFollow"
 * }
 * }</pre>
 */
public class FollowQuery
    implements Query
{
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final Database database;

  public FollowQuery(final Database database) {
    this.database = requireNonNull(database);
  }

  @Override
  public void query(final JsonObject params, final Handler<AsyncResult<JsonObject>> handler) {
    try {
      JsonObject startProperties = params.getJsonObject("start");
      requireNonNull(startProperties);
      String propertyName = requireNonNull(startProperties.getString("key"));
      String propertyValue = requireNonNull(startProperties.getString("value"));
      Direction direction = Direction.valueOf(params.getString("direction"));
      String edgeLabel = requireNonNull(params.getString("label"));
      database.readTx(g -> {
        if (g.failed()) {
          handler.handle(Future.failedFuture(g.cause()));
        }
        else {
          Graph graph = g.result();
          JsonObject result = new JsonObject();
          for (Vertex start : graph.getVertices(propertyName, propertyValue)) {
            int startNode = start.getProperty("value");
            for (Vertex adjacent : start.getVertices(direction, edgeLabel)) {
              String line = String.valueOf(startNode);
              if (Direction.IN.equals(direction)) {
                line += "<--" + edgeLabel + "--";
              }
              else if (Direction.OUT.equals(direction)) {
                line += "--" + edgeLabel + "-->";
              }
              else {
                line += "--" + edgeLabel + "--";
              }
              line += adjacent.getProperty("value");
              result.put(String.valueOf(startNode), line);
            }
          }
          handler.handle(Future.succeededFuture(result));
        }
      });
    }
    catch (Exception e) {
      log.error("Error", e);
      handler.handle(Future.failedFuture(e));
    }
  }
}
