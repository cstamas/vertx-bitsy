package org.cstamas.vertx.orientdb.examples;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.cstamas.vertx.bitsy.examples.TestVerticle;
import org.junit.Test;

public class BitsyExampleTest
    extends TestSupport
{
  @Test
  public void example(final TestContext context) throws Exception {
    Async deploy = context.async();
    vertx.deployVerticle(
        TestVerticle.class.getName(),
        new DeploymentOptions().setConfig(
            new JsonObject()
                .put("name", testName.getMethodName())
        ),
        v -> {
          if (v.failed()) {
            context.fail(v.cause());
          }
          deploy.complete();
        });
    deploy.awaitSuccess();

    // let it tick for some
    Thread.sleep(5000);
  }
}
