package org.cstamas.vertx.bitsy;

import com.lambdazen.bitsy.BitsyGraph;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Handler;

public interface Database
    extends Closeable
{
  String getName();

  Database exec(Handler<AsyncResult<BitsyGraph>> handler);

  Database readTx(Handler<AsyncResult<Graph>> handler);

  Database writeTx(Handler<AsyncResult<TransactionalGraph>> handler);
}
