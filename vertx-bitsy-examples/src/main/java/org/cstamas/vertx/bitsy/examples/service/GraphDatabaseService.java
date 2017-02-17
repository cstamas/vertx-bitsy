package org.cstamas.vertx.bitsy.examples.service;

import java.util.List;
import java.util.Map;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

@ProxyGen
public interface GraphDatabaseService
{
  static GraphDatabaseService createProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(GraphDatabaseService.class, vertx, address);
  }

  @Fluent
  GraphDatabaseService gremlinScript(Map<String, String> params,
                                     String script,
                                     Handler<AsyncResult<List<String>>> handler);
}
