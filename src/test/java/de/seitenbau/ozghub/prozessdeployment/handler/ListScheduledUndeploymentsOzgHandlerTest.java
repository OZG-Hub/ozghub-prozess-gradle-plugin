package de.seitenbau.ozghub.prozessdeployment.handler;

import static de.seitenbau.ozghub.prozessdeployment.handler.ListScheduledUndeploymentsOzgHandler.API_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.gradle.api.GradleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler.Request;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.UndeploymentHint;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import de.seitenbau.ozghub.prozessdeployment.model.response.ScheduledUndeployment;
import lombok.SneakyThrows;

public class ListScheduledUndeploymentsOzgHandlerTest extends BaseTestHandler
{
  private static final Aggregated<List<ScheduledUndeployment>> RESPONSE = constructResponse();
  private static final Aggregated<List<ScheduledUndeployment>> RESPONSE2 = constructResponse2();

  public static final String TASK_NAME = "listScheduledUndeployments";
  public static final String DATE_PATTERN = "dd.MM.yyyy";

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
    HttpHandler httpHandler = createAndStartHttpServer(RESPONSE);

    ListScheduledUndeploymentsOzgHandler sut = new ListScheduledUndeploymentsOzgHandler(createEnvironment());

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
        INFO Es sind 2 geplante Undeployments:

        DeploymentId: deploymentId1
        Undeployment Datum: 30.11.2999
        Ankündigungsnachricht:
         - Betreff: preUndeploymentSubject1
         - Text: preUndeploymentBody1
        Nachricht:
         - Betreff: undeploymentSubject1
         - Text: undeploymentBody1
        Hinweis:
         - Text: hintText1
         - Darstellung ab: 30.11.2999

        DeploymentId: deploymentId2
        Undeployment Datum: 31.12.2023
        Ankündigungsnachricht:
         - Betreff: preUndeploymentSubject2
         - Text: preUndeploymentBody2
        Nachricht:
         - Betreff: undeploymentSubject2
         - Text: undeploymentBody2
        Hinweis:
         - Text: hintText2
         - Darstellung ab: 31.12.2023
        """;
    assertThat(actualLogMessages).contains(expectedLog);
    assertThat(actualLogMessages).contains("INFO Ende des Tasks: " + TASK_NAME);
  }

  @Test
  public void listScheduledUndeployments_success2()
  {
    //arrange
    prepareLogging();
    HttpHandler httpHandler = createAndStartHttpServer(RESPONSE2);

    ListScheduledUndeploymentsOzgHandler sut = new ListScheduledUndeploymentsOzgHandler(createEnvironment());

    //act
    Aggregated<List<ScheduledUndeployment>> actual = sut.list(TASK_NAME);

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();
    assertThat(actual).usingRecursiveAssertion().isEqualTo(RESPONSE2);

    Request request = httpHandler.getRequest();
    assertRequest(request);

    List<String> actualLogMessages = listAppender.getEventList();
    assertThat(actualLogMessages).contains("INFO Start des Tasks: " + TASK_NAME);
    String expectedLog = """
        INFO Es sind 1 geplante Undeployments:

        DeploymentId: deploymentId
        Undeployment Datum: 30.11.2999
        Ankündigungsnachricht:
         - Betreff: preUndeploymentSubject
         - Text: preUndeploymentBody
        Nachricht:
         - Betreff: *Betreff nicht gesetzt*
         - Text: *Text nicht gesetzt*
        Hinweis:
         - Text: *Text nicht gesetzt*
         - Darstellung ab: *Datum nicht gesetzt*
        """;
    assertThat(actualLogMessages).contains(expectedLog);
    assertThat(actualLogMessages).contains("INFO Ende des Tasks: " + TASK_NAME);
  }

  @Test
  public void listScheduledUndeployments_error()
  {
    //arrange
    HttpHandler httpHandler =
        new HttpHandler(500, "Etwas ist schiefgelaufen".getBytes(StandardCharsets.UTF_8));
    httpServer =
        HttpServerFactory.createAndStartHttpServer(API_PATH, httpHandler);
    String url = "http://localhost:" + httpServer.getAddress().getPort();

    ListScheduledUndeploymentsOzgHandler sut = new ListScheduledUndeploymentsOzgHandler(createEnvironment());

    //act
    assertThatThrownBy(() -> sut.list(TASK_NAME))
        .isExactlyInstanceOf(GradleException.class)
        .hasMessage("Fehler: "
            + "HTTP-Response-Code: 500 Internal Server Error | Meldung des Servers: "
            + "Etwas ist schiefgelaufen | URL: " + url + API_PATH);

    //assert
    assertThat(httpHandler.getRequestCount()).isOne();

    Request request = httpHandler.getRequest();
    assertRequest(request);
  }

  private static Aggregated<List<ScheduledUndeployment>> constructResponse()
  {
    return Aggregated.complete(List.of(
        constructScheduledUndeployment("1", "30.11.2999"),
        constructScheduledUndeployment("2", "31.12.2023")
    ));
  }

  private static Aggregated<List<ScheduledUndeployment>> constructResponse2()
  {
    return Aggregated.complete(List.of(
        constructScheduledUndeploymentWithoutMessage()
    ));
  }

  @SneakyThrows
  private static ScheduledUndeployment constructScheduledUndeployment(String suffix, String dateString)
  {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    LocalDate undeploymentDate = LocalDate.parse(dateString, dateTimeFormatter);
    return new ScheduledUndeployment(
        "deploymentId" + suffix,
        undeploymentDate,
        new Message("preUndeploymentSubject" + suffix, "preUndeploymentBody" + suffix),
        new Message("undeploymentSubject" + suffix, "undeploymentBody" + suffix),
        new UndeploymentHint("hintText" + suffix, LocalDate.parse(dateString, dateTimeFormatter)));
  }

  @SneakyThrows
  private static ScheduledUndeployment constructScheduledUndeploymentWithoutMessage()
  {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    LocalDate undeploymentDate = LocalDate.parse("30.11.2999", dateTimeFormatter);
    return new ScheduledUndeployment(
        "deploymentId",
        undeploymentDate,
        new Message("preUndeploymentSubject", "preUndeploymentBody"),
        new Message(null, null),
        new UndeploymentHint(null, null));
  }

  private HttpHandler createAndStartHttpServer(Aggregated<List<ScheduledUndeployment>> response)
  {
    HttpHandler httpHandler = createHttpHandler(response);
    httpServer =
        HttpServerFactory.createAndStartHttpServer(API_PATH, httpHandler);

    return httpHandler;
  }

  @SneakyThrows
  private HttpHandler createHttpHandler(Aggregated<List<ScheduledUndeployment>> response)
  {
    byte[] responseBytes = OBJECT_MAPPER.writeValueAsBytes(response);
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
    assertThat(request.getPath()).isEqualTo(API_PATH);
    assertThat(request.getQuery()).isNull();
    assertThat(request.getHeaders()).containsEntry(HTTPHeaderKeys.CACHE_CONTROL, List.of("no-cache"));
  }

  private void prepareLogging()
  {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    listAppender = (ListAppender) config.getAppenders().get("ListAppender");
  }
}
