package org.cstamas.vertx.bitsy.examples;

import java.util.HashMap;
import java.util.stream.IntStream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.cstamas.vertx.bitsy.examples.service.GraphDatabaseService;

public class ServiceReaderVerticle
    extends AbstractVerticle
{
  private static final Logger log = LoggerFactory.getLogger(ServiceReaderVerticle.class);

  @Override
  public void start(final Future<Void> startFuture) throws Exception {
    // this works across cluster, so over the wire too
    GraphDatabaseService service = GraphDatabaseService.createProxy(vertx, "test");
    vertx.eventBus().consumer("goRead",
        (Message<JsonObject> m) -> {
          HashMap<String, String> params = new HashMap<>();
          String script = "g.V('value', $numberKind).inE('is').count()";
          IntStream.range(0, 2).forEach(number -> {
            boolean even = number % 2 == 0;
            if (even) {
              params.put("$numberKind", "even");
            }
            else {
              params.put("$numberKind", "odd");
            }
            service.gremlinScript(params, script, ar -> {
              if (ar.failed()) {
                log.error("Error", ar.cause());
              }
              else {
                int result = ar.result().getInteger("result");
                log.info("SERVICE: {} count is {}", even ? "even" : "odd", result);
              }
            });
          });
        }
    );
    super.start(startFuture);
  }
}
