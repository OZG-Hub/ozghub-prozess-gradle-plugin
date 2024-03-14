package de.seitenbau.ozghub.prozessdeployment.handler;

import static de.seitenbau.ozghub.prozessdeployment.handler.CreateScheduledUndeploymentOzgHandler.API_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler.Request;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.ScheduledUndeployment;
import de.seitenbau.ozghub.prozessdeployment.model.UndeploymentHint;
import lombok.SneakyThrows;

public class CreateScheduledUndeploymentOzgHandlerTest extends BaseTestHandler
{
  private HttpServer httpServer = null;

  private static final ScheduledUndeployment SCHEDULED_UNDEPLOYMENT = constructScheduledUndeployment();

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

    CreateScheduledUndeploymentOzgHandler sut = new CreateScheduledUndeploymentOzgHandler(createEnvironment());

    //act
    sut.createScheduledUndeploymentOzg(SCHEDULED_UNDEPLOYMENT);

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();

    Request request = httpHandler.getRequest();
    assertRequest(request);
  }

  @Test
  public void createScheduledUndeployment_error()
  {
    // arrange
    HttpHandler httpHandler =
        new HttpHandler(500, "Etwas ist schiefgelaufen".getBytes(StandardCharsets.UTF_8));
    httpServer = HttpServerFactory.createAndStartHttpServer(API_PATH, httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    CreateScheduledUndeploymentOzgHandler sut = new CreateScheduledUndeploymentOzgHandler(createEnvironment());

    // act
    assertThatThrownBy(() -> sut.createScheduledUndeploymentOzg(SCHEDULED_UNDEPLOYMENT))
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessage("Fehler beim Erstellen eines zeitgesteuerten Undeployment eines Online-Dienstes: "
            + "HTTP-Response-Code: 500 Internal Server Error | Meldung des Servers: "
            + "Etwas ist schiefgelaufen | URL: " + url + API_PATH);

    // assert
    assertThat(httpHandler.getRequestCount()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(500);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
  }

  private static ScheduledUndeployment constructScheduledUndeployment()
  {
    return new ScheduledUndeployment(
        "deploymentId",
        LocalDate.now(),
        new Message("preUndeploymentSubject", "preUndeploymentBody"),
        new Message("undeploymentSubject", "undeploymentBody"),
        new UndeploymentHint("undeploymentHintText", LocalDate.now()));
  }

  private HttpHandler createAndStartHttpServer()
  {
    HttpHandler httpHandler = createHttpHandler();
    httpServer =
        HttpServerFactory.createAndStartHttpServer(API_PATH, httpHandler);

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
    assertThat(request.getPath()).isEqualTo(API_PATH);
    assertThat(request.getQuery()).isNull();

    ScheduledUndeployment actualRequest =
        OBJECT_MAPPER.readValue(request.getRequestBody(), ScheduledUndeployment.class);
    assertThat(actualRequest).usingRecursiveAssertion().isEqualTo(SCHEDULED_UNDEPLOYMENT);
  }
}
