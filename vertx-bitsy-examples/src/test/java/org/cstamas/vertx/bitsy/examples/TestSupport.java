package org.cstamas.vertx.bitsy.examples;

import io.vertx.core.Vertx;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

/**
 * Junit test.
 */
@RunWith(VertxUnitRunner.class)
public abstract class TestSupport
{
  static {
    System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
  }

  @Rule
  public TestName testName = new TestName();

  protected Vertx vertx;

  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = Vertx.vertx();
    // https://github.com/eclipse/vert.x/issues/1625
    // vertx.exceptionHandler(context.exceptionHandler());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }
}
