package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler.Request;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.ScheduledUndeployment;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import lombok.SneakyThrows;

public class ListScheduledUndeploymentsHandlerTest extends BaseTestHandler
{
  private static final Aggregated<List<ScheduledUndeployment>> RESPONSE = constructResponse();

  public static final String TASK_NAME = "listScheduledUndeployments";

  private HttpServer httpServer = null;

  private ListAppender listAppender;

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
    prepareLogging();
    HttpHandler httpHandler = createAndStartHttpServer();

    ListScheduledUndeploymentsHandler sut = new ListScheduledUndeploymentsHandler(createEnvironment());

    //act
    Aggregated<List<ScheduledUndeployment>> actual = sut.list(TASK_NAME);

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();
    assertThat(actual).usingRecursiveAssertion().isEqualTo(RESPONSE);

    Request request = httpHandler.getRequest();
    assertRequest(request);

    List<String> actualLogMessages = listAppender.getEventList();
    assertThat(actualLogMessages).contains("INFO Start des Tasks: " + TASK_NAME);
    String expectedLog = """
        INFO Es sind 2 geplanten Undeployments:
        DeploymentId: deploymentId1
        Undeployment Datum: 10.02.2999
        Ankündigungsnachricht:
         - Betreff: preUndeploymentSubject1
         - Text: preUndeploymentBody1
        Nachricht:
         - Betreff: undeploymentSubject1
         - Text: undeploymentBody1

        DeploymentId: deploymentId2
        Undeployment Datum: 11.02.2999
        Ankündigungsnachricht:
         - Betreff: preUndeploymentSubject2
         - Text: preUndeploymentBody2
        Nachricht:
         - Betreff: undeploymentSubject2
         - Text: undeploymentBody2
        """;
    assertThat(actualLogMessages).contains(expectedLog);
    assertThat(actualLogMessages).contains("INFO Ende des Tasks: " + TASK_NAME);
  }

  private static Aggregated<List<ScheduledUndeployment>> constructResponse()
  {
    return Aggregated.complete(List.of(
        constructScheduledUndeployment("1", "10.02.2999"),
        constructScheduledUndeployment("2", "11.02.2999")
    ));
  }

  @SneakyThrows
  private static ScheduledUndeployment constructScheduledUndeployment(String suffix, String dateString)
  {
    SimpleDateFormat formatter = new SimpleDateFormat("MM.dd.yyyy");
    Date date = formatter.parse(dateString);
    return new ScheduledUndeployment(
        "deploymentId" + suffix,
        date,
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

  private void prepareLogging()
  {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    listAppender = (ListAppender) config.getAppenders().get("ListAppender");
  }
}
