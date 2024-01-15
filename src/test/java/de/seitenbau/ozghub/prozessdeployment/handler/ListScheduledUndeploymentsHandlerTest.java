package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler.Request;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.request.Message;
import de.seitenbau.ozghub.prozessdeployment.model.request.ScheduledUndeployment;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import lombok.SneakyThrows;

public class ListScheduledUndeploymentsHandlerTest extends BaseTestHandler
{
  private static final Aggregated<List<ScheduledUndeployment>> RESPONSE = constructResponse();

  private HttpServer httpServer = null;

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
  public void listScheduledUndeployments_success()
  {
    //arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    ListScheduledUndeploymentsHandler sut = new ListScheduledUndeploymentsHandler(createEnvironment());

    //act
    Aggregated<List<ScheduledUndeployment>> actual = sut.listScheduledUndeployments();

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();
    assertThat(actual).usingRecursiveAssertion().isEqualTo(RESPONSE);

    Request request = httpHandler.getRequest();
    assertRequest(request);
  }

  private static Aggregated<List<ScheduledUndeployment>> constructResponse()
  {
    return Aggregated.complete(List.of(
        constructScheduledUndeployment("1"),
        constructScheduledUndeployment("2")
    ));
  }

  private static ScheduledUndeployment constructScheduledUndeployment(String suffix)
  {
    return new ScheduledUndeployment(
        "deploymentId" + suffix,
        new Date(),
        new Message("preUndeploymentSubject" + suffix, "preUndeploymentBody" + suffix),
        new Message("undeploymentSubject" + suffix, "undeploymentBody" + suffix));
  }

  private HttpHandler createAndStartHttpServer()
  {
    HttpHandler httpHandler = createHttpHandler();
    httpServer =
        HttpServerFactory.createAndStartHttpServer(ListScheduledUndeploymentsHandler.API_PATH, httpHandler);

    return httpHandler;
  }

  @SneakyThrows
  private HttpHandler createHttpHandler()
  {
    byte[] responseBytes = OBJECT_MAPPER.writeValueAsBytes(RESPONSE);
    return new HttpHandler(200, responseBytes);
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
    assertThat(request.getRequestMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo(ListScheduledUndeploymentsHandler.API_PATH);
    assertThat(request.getQuery()).isNull();
  }
}
