package org.cstamas.vertx.bitsy.rom;

import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import org.cstamas.vertx.bitsy.ConnectionOptions;
import org.cstamas.vertx.bitsy.Database;
import org.cstamas.vertx.bitsy.Manager;
import org.cstamas.vertx.bitsy.ManagerOptions;
import org.cstamas.vertx.bitsy.rom.service.RomDatabase;
import org.cstamas.vertx.bitsy.rom.service.impl.RomDatabaseImpl;

public class BitsyRomVerticle
    extends AbstractVerticle
{
  private int serviceTimeoutSeconds;

  private String nodeId;

  private ManagerOptions managerOptions;

  private Manager manager;

  private Database database;

  private MessageConsumer<JsonObject> service;

  @Override
  public void start(final Future<Void> startFuture) throws Exception {
    serviceTimeoutSeconds = config().getInteger("serviceTimeoutSeconds", 60);
    nodeId = config().getString("nodeId", UUID.randomUUID().toString());

    managerOptions = ManagerOptions.fromJsonObject(config());
    manager = Manager.create(vertx, managerOptions);
    manager.open(opened -> {
      if (opened.failed()) {
        startFuture.fail(opened.cause());
      }
      else {
        setUpDatabase(startFuture.completer());
      }
    });

    vertx.eventBus().consumer("reload", (Message<JsonObject> reloadMessage) -> {
    });
  }

  @Override
  public void stop(final Future<Void> stopFuture) throws Exception {
    tearDownDatabase(isDown -> {
      if (isDown.failed()) {
        stopFuture.fail(isDown.cause());
      }
      else {
        manager.close(stopFuture.completer());
      }
    });
  }

  private void reload(final Handler<AsyncResult<Void>> handler) {
    tearDownDatabase(isDown -> {
      if (isDown.failed()) {
        handler.handle(Future.failedFuture(isDown.cause()));
      }
      else {
        setUpDatabase(handler);
      }
    });
  }

  private void setUpDatabase(final Handler<AsyncResult<Void>> handler) {
    ConnectionOptions connectionOptions = new ConnectionOptions.Builder(nodeId).build();
    manager.create(
        connectionOptions,
        b -> {
          // prepare schema, create indexes
        },
        created -> {
          if (created.failed()) {
            handler.handle(Future.failedFuture(created.cause()));
          }
          else {
            manager.get(connectionOptions.name(), ab -> {
              if (ab.failed()) {
                handler.handle(Future.failedFuture(ab.cause()));
              }
              else {
                database = ab.result();
                RomDatabase romDatabase = new RomDatabaseImpl(nodeId, database);
                service = ProxyHelper.registerService(
                    RomDatabase.class,
                    vertx,
                    romDatabase,
                    RomDatabase.ADDRESS,
                    serviceTimeoutSeconds
                );
                handler.handle(Future.succeededFuture());
              }
            });
          }
        }
    );
  }

  private void tearDownDatabase(final Handler<AsyncResult<Void>> handler) {
    if (service != null) {
      ProxyHelper.unregisterService(service);
      service = null;
    }
    if (database != null) {
      database.close(closed -> {
        if (closed.failed()) {
          handler.handle(Future.failedFuture(closed.cause()));
        }
        else {
          database = null;
          handler.handle(Future.succeededFuture());
        }
      });
    }
    else {
      handler.handle(Future.succeededFuture());
    }
  }
}
