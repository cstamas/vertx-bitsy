package org.cstamas.vertx.bitsy.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import com.tinkerpop.blueprints.Vertex;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ProxyHelper;
import org.cstamas.vertx.bitsy.ConnectionOptions;
import org.cstamas.vertx.bitsy.Database;
import org.cstamas.vertx.bitsy.Manager;
import org.cstamas.vertx.bitsy.ManagerOptions;
import org.cstamas.vertx.bitsy.rom.service.Query;
import org.cstamas.vertx.bitsy.rom.service.RomDatabase;
import org.cstamas.vertx.bitsy.rom.service.support.RomDatabaseImpl;

public class TestVerticle
    extends AbstractVerticle
{
  private static final Logger log = LoggerFactory.getLogger(TestVerticle.class);

  private Manager manager;

  private int counter;

  @Override
  public void start(final Future<Void> startFuture) throws Exception {
    this.manager = Manager.create(vertx, ManagerOptions.fromJsonObject(config()));
    manager.open(opened -> {
      if (opened.failed()) {
        startFuture.fail(opened.cause());
      }
      else {
        ConnectionOptions connectionOptions = selectConnectionInfo();
        manager.create(
            connectionOptions,
            bh -> {
              Set<String> indexes = bh.getIndexedKeys(Vertex.class);
              if (!indexes.contains("type")) {
                bh.createKeyIndex("type", Vertex.class);
              }
              if (!indexes.contains("value")) {
                bh.createKeyIndex("value", Vertex.class);
              }
            },
            created -> {
              if (created.failed()) {
                startFuture.fail(created.cause());
              }
              else {
                manager.get(connectionOptions.name(), adb -> {
                  if (adb.failed()) {
                    startFuture.fail(adb.cause());
                  }
                  else {
                    Database database = adb.result();
                    Map<String, Query> queries = new HashMap<>();
                    queries.put("follow", new FollowQuery(database));

                    // install service, this may happen on remote cluster member too
                    ProxyHelper.registerService(
                        RomDatabase.class,
                        vertx,
                        new RomDatabaseImpl(UUID.randomUUID().toString(), database, queries),
                        "test"
                    );

                    // deploy test verticles, some of them we pre-created, some of them are self-sufficient
                    ReaderVerticle readerVerticle = new ReaderVerticle(database);
                    WriterVerticle writerVerticle = new WriterVerticle(database);

                    ArrayList<Future> deploys = new ArrayList<>(4);
                    IntStream.range(0, 4).forEach(i -> deploys.add(Future.future()));
                    vertx.deployVerticle(readerVerticle, deploys.get(0).completer());
                    vertx.deployVerticle(writerVerticle, deploys.get(1).completer());
                    vertx.deployVerticle(ServiceGremlinReaderVerticle.class.getName(), deploys.get(2).completer());
                    vertx.deployVerticle(ServiceQueryReaderVerticle.class.getName(), deploys.get(3).completer());
                    CompositeFuture.all(deploys).setHandler(cf -> {
                      if (cf.failed()) {
                        startFuture.fail(cf.cause());
                      }
                      else {
                        // set "ticky-ticker", let the games begin
                        vertx.setPeriodic(300, t -> {
                          vertx.eventBus().publish("goRead", new JsonObject());
                        });
                        vertx.setPeriodic(300, t -> {
                          counter++;
                          vertx.eventBus().publish("goWrite", new JsonObject().put("number", counter));
                        });
                        startFuture.complete();
                      }
                    });
                  }
                });
              }
            });
      }
    });
  }

  private ConnectionOptions selectConnectionInfo() {
    String name = Objects.requireNonNull(config().getString("name"));
    return manager.connection(name).build();
  }
}
