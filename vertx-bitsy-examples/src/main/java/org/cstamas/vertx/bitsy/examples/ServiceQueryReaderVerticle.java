package org.cstamas.vertx.bitsy.examples;

import com.tinkerpop.blueprints.Direction;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ProxyHelper;
import org.cstamas.vertx.bitsy.rom.service.RomDatabase;

public class ServiceQueryReaderVerticle
    extends AbstractVerticle
{
  private static final Logger log = LoggerFactory.getLogger(ServiceQueryReaderVerticle.class);

  @Override
  public void start(final Future<Void> startFuture) throws Exception {
    // this works across cluster, so over the wire too
    RomDatabase service = ProxyHelper.createProxy(RomDatabase.class, vertx, "test");
    vertx.eventBus().consumer("goRead",
        (Message<JsonObject> m) -> {
          JsonObject params = new JsonObject();
          params.put("start", new JsonObject().put("key", "type").put("value", "number"));
          params.put("direction", Direction.OUT.name());
          params.put("label", "is");
          service.query("follow", params, ar -> {
            if (ar.failed()) {
              log.error("Error", ar.cause());
            }
            else {
              log.info("QUERY: {} result {}", params, ar.result());
            }
          });
        }
    );
    super.start(startFuture);
  }
}
