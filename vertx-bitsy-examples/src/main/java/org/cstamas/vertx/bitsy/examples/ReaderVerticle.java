package org.cstamas.vertx.bitsy.examples;

import java.util.stream.StreamSupport;

import com.tinkerpop.blueprints.Graph;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.cstamas.vertx.bitsy.Database;

public class ReaderVerticle
    extends AbstractVerticle
{
  private static final Logger log = LoggerFactory.getLogger(ReaderVerticle.class);

  private final Database database;

  public ReaderVerticle(final Database database) {
    this.database = database;
  }

  @Override
  public void start(final Future<Void> startFuture) throws Exception {
    vertx.eventBus().consumer("goRead",
        (Message<JsonObject> m) -> {
          database.readTx(ag -> {
            if (ag.failed()) {
              m.fail(500, ag.cause().getMessage());
            }
            else {
              Graph graph = ag.result();

              long count = StreamSupport.stream(graph.getVertices("type", "number").spliterator(), false).count();
              log.info("READ: {} numbers so far", count);
            }
          });
        });
    super.start(startFuture);
  }
}
