package de.seitenbau.ozghub.prozessdeployment.handler;

import static de.seitenbau.ozghub.prozessdeployment.handler.DeleteScheduledUndeploymentOzgHandler.API_PATH;
import static de.seitenbau.ozghub.prozessdeployment.handler.DeleteScheduledUndeploymentOzgHandler.getApiPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler.Request;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import lombok.SneakyThrows;

public class DeleteScheduledUndeploymentOzgHandlerTest extends BaseTestHandler
{
  public static final String DEPLOYMENT_ID = "deploymentId";
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
    HttpHandler httpHandler = createAndStartHttpServer(DEPLOYMENT_ID);

    DeleteScheduledUndeploymentOzgHandler sut =
        new DeleteScheduledUndeploymentOzgHandler(createEnvironment());

    //act
    sut.deleteScheduledUndeployment(DEPLOYMENT_ID);

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();

    Request request = httpHandler.getRequest();
    assertRequest(request, DEPLOYMENT_ID);
  }

  @Test
  public void deleteScheduledUndeployment_success_urlEncoding()
  {
    //arrange
    HttpHandler httpHandler = createAndStartHttpServer("?hack=1");

    DeleteScheduledUndeploymentOzgHandler sut =
        new DeleteScheduledUndeploymentOzgHandler(createEnvironment());

    //act
    sut.deleteScheduledUndeployment("?hack=1");

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();
    assertThat(httpHandler.getResponseCode()).isEqualTo(204);

    Request request = httpHandler.getRequest();
    assertRequest(request, "?hack=1");
  }

  @Test
  public void deleteScheduledUndeployment_error()
  {
    //arrange
    HttpHandler httpHandler =
        new HttpHandler(500, "Etwas ist schiefgelaufen".getBytes(StandardCharsets.UTF_8));
    httpServer =
        HttpServerFactory.createAndStartHttpServer(getApiPath(DEPLOYMENT_ID), httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    DeleteScheduledUndeploymentOzgHandler sut =
        new DeleteScheduledUndeploymentOzgHandler(createEnvironment());

    //act
    assertThatThrownBy(() -> sut.deleteScheduledUndeployment(DEPLOYMENT_ID))
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessage("Fehler beim Löschen eines zeitgesteuerten Undeployments eines Online-Dienstes: "
            + "HTTP-Response-Code: 500 Internal Server Error | Meldung des Servers: "
            + "Etwas ist schiefgelaufen | URL: " + url + getApiPath(DEPLOYMENT_ID));

    // assert
    assertThat(httpHandler.getRequestCount()).isOne();
    assertThat(httpHandler.getResponseCode()).isEqualTo(500);

    Request request = httpHandler.getRequest();
    assertRequest(request, DEPLOYMENT_ID);
  }


  private HttpHandler createAndStartHttpServer(String deploymentId)
  {
    HttpHandler httpHandler = createHttpHandler();
    httpServer =
        HttpServerFactory.createAndStartHttpServer(API_PATH.formatted(deploymentId), httpHandler);

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
    assertThat(request.getRawPath()).isEqualTo(getApiPath(deploymentId));
    assertThat(request.getQuery()).isNull();
  }
}
