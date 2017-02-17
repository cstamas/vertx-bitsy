package org.cstamas.vertx.bitsy.impl;

import com.lambdazen.bitsy.BitsyGraph;
import com.lambdazen.bitsy.BitsyIsolationLevel;
import com.lambdazen.bitsy.ThreadedBitsyGraph;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.util.wrappers.readonly.ReadOnlyGraph;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.cstamas.vertx.bitsy.Database;

import static java.util.Objects.requireNonNull;

public class BitsyDatabase
    implements Database
{
  private final String name;

  private final ManagerImpl manager;

  BitsyDatabase(final String name, final ManagerImpl manager) {
    this.name = requireNonNull(name);
    this.manager = requireNonNull(manager);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Database exec(final Handler<AsyncResult<BitsyGraph>> handler) {
    manager.exec(name, handler);
    return this;
  }

  @Override
  public Database readTx(final Handler<AsyncResult<Graph>> handler) {
    manager.exec(name, g -> {
      if (g.failed()) {
        handler.handle(Future.failedFuture(g.cause()));
      }
      else {
        BitsyGraph bitsyGraph = g.result();
        ThreadedBitsyGraph tx = (ThreadedBitsyGraph) bitsyGraph.newTransaction();
        tx.setTxIsolationLevel(BitsyIsolationLevel.REPEATABLE_READ);
        try {
          handler.handle(Future.succeededFuture(new ReadOnlyGraph<>(tx)));
        }
        finally {
          tx.shutdown();
        }
      }
    });
    return this;
  }

  @Override
  public Database writeTx(final Handler<AsyncResult<TransactionalGraph>> handler) {
    manager.exec(name, g -> {
      if (g.failed()) {
        handler.handle(Future.failedFuture(g.cause()));
      }
      else {
        BitsyGraph bitsyGraph = g.result();
        ThreadedBitsyGraph tx = (ThreadedBitsyGraph) bitsyGraph.newTransaction();
        tx.setTxIsolationLevel(BitsyIsolationLevel.REPEATABLE_READ);
        try {
          handler.handle(Future.succeededFuture(tx));
        }
        finally {
          tx.shutdown();
        }
      }
    });
    return this;
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> handler) {
    manager.close(getName(), handler);
  }
}
