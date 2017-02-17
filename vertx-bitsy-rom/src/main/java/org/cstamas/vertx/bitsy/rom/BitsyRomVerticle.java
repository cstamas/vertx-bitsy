package org.cstamas.vertx.bitsy.rom;

import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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
        setUpDatabase(startFuture);
      }
    });
  }

  private void setUpDatabase(final Future<Void> future) {
    ConnectionOptions connectionOptions = new ConnectionOptions.Builder(nodeId).build();
    manager.create(
        connectionOptions,
        b -> {
          // prepare schema, create indexes
        },
        created -> {
          if (created.failed()) {
            future.fail(created.cause());
          }
          else {
            manager.get(connectionOptions.name(), ab -> {
              if (ab.failed()) {
                future.fail(ab.cause());
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
                future.complete();
              }
            });
          }
        }
    );
  }

  private void tearDownDatabase(final Future<Void> future) {
    if (service != null) {
      ProxyHelper.unregisterService(service);
      service = null;
    }
    Future<Void> closeFuture = Future.future();
    if (database != null) {
      database.close(closeFuture.completer());
    }
    else {
      closeFuture.complete();
    }
    closeFuture.setHandler(future.completer());
  }
}
