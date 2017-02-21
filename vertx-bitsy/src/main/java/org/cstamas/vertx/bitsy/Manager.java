package org.cstamas.vertx.bitsy;

import com.lambdazen.bitsy.BitsyGraph;
import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.cstamas.vertx.bitsy.impl.ManagerImpl;

public interface Manager
    extends Closeable
{
  static Manager create(Vertx vertx, ManagerOptions managerOptions) {
    return new ManagerImpl(vertx, managerOptions);
  }

  Manager open(Handler<AsyncResult<Void>> handler);

  ConnectionOptions.Builder connection(String name);

  Manager create(ConnectionOptions connectionOptions,
                 Handler<BitsyGraph> handler,
                 Handler<AsyncResult<Void>> resultHandler);

  Manager get(String name, Handler<AsyncResult<Database>> handler);
}
