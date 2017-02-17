package org.cstamas.vertx.bitsy.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import com.lambdazen.bitsy.BitsyGraph;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.cstamas.vertx.bitsy.ConnectionOptions;
import org.cstamas.vertx.bitsy.ConnectionOptions.Builder;
import org.cstamas.vertx.bitsy.Database;
import org.cstamas.vertx.bitsy.Manager;
import org.cstamas.vertx.bitsy.ManagerOptions;

import static java.util.Objects.requireNonNull;

public class ManagerImpl
    implements Manager
{
  private static class DatabaseInfo
  {
    private final BitsyGraph bitsyGraph;

    DatabaseInfo(final BitsyGraph bitsyGraph)
    {
      this.bitsyGraph = bitsyGraph;
    }

    void close() {
      bitsyGraph.shutdown();
    }
  }

  private static final Logger log = LoggerFactory.getLogger(ManagerImpl.class);

  private final Context context;

  private final ManagerOptions managerOptions;

  private final HashMap<String, DatabaseInfo> databaseInfos;

  private Path bitsyHome;

  public ManagerImpl(final Context context, final ManagerOptions managerOptions)
  {
    this.context = requireNonNull(context);
    this.managerOptions = requireNonNull(managerOptions);
    this.databaseInfos = new HashMap<>();
  }

  private <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler, Handler<AsyncResult<T>> resultHandler) {
    context.executeBlocking(blockingCodeHandler, false, resultHandler);
  }

  @Override
  public Manager open(final Handler<AsyncResult<Void>> handler) {
    executeBlocking(
        v -> {
          try {
            open();
            handler.handle(Future.succeededFuture());
          }
          catch (Exception e) {
            handler.handle(Future.failedFuture(e));
          }
        },
        handler
    );
    return null;
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> handler) {
    executeBlocking(
        f -> {
          try {
            closeManager();
            log.info("Bitsy shutdown");
            f.complete();
          }
          catch (Exception e) {
            f.fail(e);
          }
        },
        handler
    );
  }

  @Override
  public Builder connection(final String name) {
    return new ConnectionOptions.Builder(name);
  }

  @Override
  public Manager create(final ConnectionOptions connectionOptions,
                        final Handler<BitsyGraph> handler,
                        final Handler<AsyncResult<Void>> resultHandler)
  {
    executeBlocking(
        f -> {
          try {
            synchronized (databaseInfos) {
              final Path bitsyPath = bitsyHome.resolve(connectionOptions.name());
              if (!Files.isDirectory(bitsyPath)) {
                Files.createDirectory(bitsyPath);
              }
              BitsyGraph bitsyGraph = new BitsyGraph(bitsyPath);
              handler.handle(bitsyGraph);
              DatabaseInfo info = new DatabaseInfo(bitsyGraph);
              databaseInfos.put(connectionOptions.name(), info);
              f.complete();
            }
          }
          catch (Exception e) {
            f.fail(e);
          }
        },
        resultHandler
    );
    return this;
  }

  @Override
  public Manager get(final String name,
                     final Handler<AsyncResult<Database>> handler)
  {
    executeBlocking(
        f -> {
          try {
            DatabaseInfo databaseInfo = databaseInfos.get(name);
            if (databaseInfo == null) {
              f.fail(new IllegalArgumentException("Non existent database:" + name));
            }
            else {
              f.complete(new BitsyDatabase(name, this));
            }
          }
          catch (Exception e) {
            f.fail(e);
          }
        },
        handler
    );
    return this;
  }

  private void open() throws Exception {
    try {
      openManager();
      log.info("Bitsy manager started");
    }
    catch (Exception e) {
      log.error("Could not open database", e);
      throw e;
    }
  }

  private void openManager() throws IOException {
    this.bitsyHome = Paths.get(managerOptions.getHome()).toFile().getCanonicalFile().toPath();
    if (!Files.isDirectory(bitsyHome)) {
      Files.createDirectories(bitsyHome);
    }
  }

  private void closeManager() {
    databaseInfos.values().forEach(DatabaseInfo::close);
    databaseInfos.clear();
  }

  void exec(final String name, final Handler<AsyncResult<BitsyGraph>> handler) {
    context.runOnContext(v -> {
      try {
        DatabaseInfo databaseInfo = databaseInfos.get(name);
        if (databaseInfo == null) {
          IllegalArgumentException iaex = new IllegalArgumentException("Non existent database:" + name);
          handler.handle(Future.failedFuture(iaex));
        }
        else {
          BitsyGraph graph = databaseInfo.bitsyGraph;
          handler.handle(Future.succeededFuture(graph));
        }
      }
      catch (Exception e) {
        handler.handle(Future.failedFuture(e));
      }
    });
  }

  void close(final String name, final Handler<AsyncResult<Void>> handler) {
    executeBlocking(
        f -> {
          try {
            synchronized (databaseInfos) {
              DatabaseInfo databaseInfo = databaseInfos.get(name);
              if (databaseInfo == null) {
                f.fail(new IllegalArgumentException("Non existent database:" + name));
              }
              else {
                databaseInfo.close();
                databaseInfos.remove(name);
                f.complete();
              }
            }
          }
          catch (Exception e) {
            f.fail(e);
          }
        },
        handler
    );
  }
}
