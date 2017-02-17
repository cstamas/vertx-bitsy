package org.cstamas.vertx.bitsy.examples;

import java.util.NoSuchElementException;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.cstamas.vertx.bitsy.Database;

public class WriterVerticle
    extends AbstractVerticle
{
  private static final Logger log = LoggerFactory.getLogger(WriterVerticle.class);

  private final Database database;

  public WriterVerticle(final Database database) {
    this.database = database;
  }

  @Override
  public void start(final Future<Void> startFuture) throws Exception {
    vertx.eventBus().consumer("goWrite",
        (Message<JsonObject> m) -> {
          database.writeTx(atg -> {
            if (atg.failed()) {
              m.fail(500, atg.cause().getMessage());
            }
            else {
              handle(atg.result(), m.body().getInteger("number"));
            }
          });
        });
    super.start(startFuture);
  }

  private void handle(final TransactionalGraph tg, int number) {
    Vertex newVertex = tg.addVertex(null);
    newVertex.setProperty("type", "number");
    newVertex.setProperty("value", number);

    Vertex parity = parity(tg, number % 2 == 0);
    tg.addEdge(null, newVertex, parity, "is");
    tg.commit();
    log.info("WRITE: Wrote number {}", number);
  }

  private Vertex parity(final TransactionalGraph tg, boolean even) {
    String kind = even ? "even" : "odd";
    try {
      return tg.getVertices("value", kind).iterator().next();
    }
    catch (NoSuchElementException e) {
      Vertex parity = tg.addVertex(null);
      parity.setProperty("type", "parity");
      parity.setProperty("value", kind);
      return parity;
    }
  }
}
