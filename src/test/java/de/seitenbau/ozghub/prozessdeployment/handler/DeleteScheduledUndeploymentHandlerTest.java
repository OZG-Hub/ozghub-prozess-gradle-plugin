package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler.Request;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import lombok.SneakyThrows;

public class DeleteScheduledUndeploymentHandlerTest extends BaseTestHandler
{
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
  public void deleteScheduledUndeployment_success()
  {
    //arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    DeleteScheduledUndeploymentHandler sut = new DeleteScheduledUndeploymentHandler(createEnvironment());

    //act
    sut.deleteScheduledUndeployment("deploymentId");

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();

    Request request = httpHandler.getRequest();
    assertRequest(request, "deploymentId");
  }

  @Test
  public void deleteScheduledUndeployment_success_urlEncoding()
  {
    //arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    DeleteScheduledUndeploymentHandler sut = new DeleteScheduledUndeploymentHandler(createEnvironment());

    //act
    sut.deleteScheduledUndeployment("?hack=1");

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();

    Request request = httpHandler.getRequest();
    assertRequest(request, "?hack=1");
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
  private void assertRequest(Request request, String deploymentId)
  {
    assertThat(request.getRequestMethod()).isEqualTo("DELETE");
    assertThat(request.getPath()).isEqualTo(DeleteScheduledUndeploymentHandler.API_PATH + "/" + deploymentId);
    assertThat(request.getQuery()).isNull();
  }
}
