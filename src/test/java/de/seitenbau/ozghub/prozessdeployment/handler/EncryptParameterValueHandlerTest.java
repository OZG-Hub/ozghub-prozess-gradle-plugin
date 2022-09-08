package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.response.EncryptParameterValueResponse;
import lombok.SneakyThrows;

public class EncryptParameterValueHandlerTest
{
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static final String TASK_NAME = "encryptParameterValue";

  private HttpServer httpServer = null;

  private ListAppender listAppender;

  @AfterEach
  public void after()
  {
    if (httpServer != null)
    {
      httpServer.stop(0);
    }
  }

  @Test
  public void encryptParameterValue()
  {
    // arrange
    prepareLogging();

    String processKey = "m1.prozessDefinitionKey";
    String parameterValue = "test value";

    EncryptParameterValueResponse encryptParameterValueResponse = EncryptParameterValueResponse.builder()
        .encryptedParameterValue("ozghub:cu:TEST_test_Test")
        .build();

    HttpHandler httpHandler = createAndStartHttpServer(encryptParameterValueResponse);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    EncryptParameterValueHandler sut =
        new EncryptParameterValueHandler(env, processKey, parameterValue);

    // act
    sut.encryptParameterValue();

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env);

    int port = httpServer.getAddress().getPort();

    List<String> actualLogMessages = listAppender.getEventList();
    assertThat(actualLogMessages).contains("INFO Start des Tasks: " + TASK_NAME);
    assertThat(actualLogMessages).contains("INFO Sende POST-Request an http://localhost:" + port
        + "/prozessparameter/parameter/encryptParameterValue");
    assertEncryptParameterValueLogMessage(actualLogMessages, parameterValue);
    assertThat(actualLogMessages).contains("INFO Ende des Tasks: " + TASK_NAME);
  }

  private void prepareLogging()
  {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    listAppender = (ListAppender) config.getAppenders().get("ListAppender");
  }

  private void assertEncryptParameterValueLogMessage(List<String> actualLogMessages, String parameterValue)
  {
    String expectedLogMessage1 = "INFO Die Verschluesselung des Parameterwertes '" + parameterValue +
        "' wurde erfolgreich abgeschlossen.";
    String expectedLogMessage2 = "INFO Der verschluesselte Parameterwert ist: ozghub:cu:TEST_test_Test";
    assertThat(actualLogMessages).contains(expectedLogMessage1);
    assertThat(actualLogMessages).contains(expectedLogMessage2);
  }

  private void assertRequest(HttpHandler.Request request)
  {
    assertThat(request.getRequestMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo(EncryptParameterValueHandler.API_PATH);
    assertThat(request.getQuery()).isNull();
  }

  private void assertRequestHeaders(HttpHandler.Request request, Environment env)
  {
    Map<String, List<String>> headers = request.getHeaders();
    assertThat(headers).containsEntry(HTTPHeaderKeys.CONTENT_TYPE, List.of("application/json"));

    String tmp = env.getUser() + ':' + env.getPassword();
    String auth = "Basic " + Base64.getEncoder().encodeToString(tmp.getBytes(StandardCharsets.UTF_8));
    assertThat(headers).containsEntry(HTTPHeaderKeys.AUTHORIZATION, List.of(auth));
  }

  private HttpHandler createAndStartHttpServer(EncryptParameterValueResponse encryptParameterValueResponse)
  {
    HttpHandler httpHandler = createHttpHandler(encryptParameterValueResponse);
    httpServer =
        HttpServerFactory.createAndStartHttpServer(EncryptParameterValueHandler.API_PATH, httpHandler);

    return httpHandler;
  }

  @SneakyThrows
  private HttpHandler createHttpHandler(EncryptParameterValueResponse encryptParameterValueResponse)
  {
    return new HttpHandler(200, OBJECT_MAPPER.writeValueAsBytes(encryptParameterValueResponse));
  }
}
