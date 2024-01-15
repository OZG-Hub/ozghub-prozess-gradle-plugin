package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler.Request;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.ScheduledUndeployment;
import lombok.SneakyThrows;

public class CreateScheduledUndeploymentHandlerTest extends BaseTestHandler
{
  private HttpServer httpServer = null;

  private static final ScheduledUndeployment SCHEDULED_UNDEPLOYMENT_1 = constructScheduledUndeployment();

  @Override
  protected File getTestFolder()
  {
    return null;
  }

  @AfterEach
  public void after()
  {
    if (httpServer != null)
    {
      httpServer.stop(0);
    }
  }

  @Test
  public void createScheduledUndeployment_success()
  {
    //arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    CreateScheduledUndeploymentHandler sut = new CreateScheduledUndeploymentHandler(createEnvironment());

    //act
    sut.createScheduledUndeployment(SCHEDULED_UNDEPLOYMENT_1);

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();

    Request request = httpHandler.getRequest();
    assertRequest(request);
  }

  private static ScheduledUndeployment constructScheduledUndeployment()
  {
    return new ScheduledUndeployment(
        "deploymentId",
        new Date(),
        new Message("preUndeploymentSubject", "preUndeploymentBody"),
        new Message("undeploymentSubject", "undeploymentBody"));
  }

  private HttpHandler createAndStartHttpServer()
  {
    HttpHandler httpHandler = createHttpHandler();
    httpServer =
        HttpServerFactory.createAndStartHttpServer(CreateScheduledUndeploymentHandler.API_PATH, httpHandler);

    return httpHandler;
  }

  private HttpHandler createHttpHandler()
  {
    return new HttpHandler(204, new byte[0]);
  }

  private Environment createEnvironment()
  {
    return new Environment(getUrl(), "foo1", "bar1");
  }

  private String getUrl()
  {
    return "http://localhost:" + httpServer.getAddress().getPort();
  }

  @SneakyThrows
  private void assertRequest(Request request)
  {
    assertThat(request.getRequestMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo(CreateScheduledUndeploymentHandler.API_PATH);
    assertThat(request.getQuery()).isNull();

    ScheduledUndeployment actualRequest =
        OBJECT_MAPPER.readValue(request.getRequestBody(), ScheduledUndeployment.class);
    assertThat(actualRequest).usingRecursiveAssertion().isEqualTo(SCHEDULED_UNDEPLOYMENT_1);
  }
}
