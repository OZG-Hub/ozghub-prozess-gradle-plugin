package de.seitenbau.ozghub.prozesspipeline.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozesspipeline.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozesspipeline.model.response.ProcessDeployment;
import de.seitenbau.ozghub.prozesspipeline.model.response.ProcessDeploymentList;
import lombok.SneakyThrows;

public class ListProcessesHandlerTest
{
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String DATE_FORMAT = "YYYY-MM-dd hh:mm:ss";
  public static final String TASK_NAME = "listProcesses";

  private HttpServer httpServer = null;

  private ListProcessesHandler sut;

  private ListAppender listAppender;

  @AfterEach
  private void after()
  {
    if (httpServer != null)
    {
      httpServer.stop(0);
    }
  }

  @Test
  public void listProcesses()
  {
    // arrange
    prepareLogging();

    Date deploymentDate = new Date();

    ProcessDeploymentList deploymentList = prepareDeployment(deploymentDate);

    HttpHandler httpHandler = createAndStartHttpServer(deploymentList);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new ListProcessesHandler(env);

    // act
    sut.list("listProcesses");

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env);

    List<String> actualLogMessages = listAppender.getEventList();
    assertThat(actualLogMessages).contains("INFO Start des Tasks: " + TASK_NAME);
    assertDeploymentLogMessage(actualLogMessages, deploymentDate, deploymentList.getValue().get(0));
    assertThat(actualLogMessages).contains("INFO Ende des Tasks: " + TASK_NAME);
  }

  @Test
  public void listProcesses_deploymentList_incomplete()
  {
    // arrange
    prepareLogging();

    Date deploymentDate = new Date();

    ProcessDeploymentList deploymentList = prepareDeployment(deploymentDate);
    deploymentList.setComplete(false);

    HttpHandler httpHandler = createAndStartHttpServer(deploymentList);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new ListProcessesHandler(env);

    // act
    sut.list(TASK_NAME);

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env);

    List<String> actualLogMessages = listAppender.getEventList();
    assertThat(actualLogMessages).contains("INFO Start des Tasks: " + TASK_NAME);
    assertDeploymentLogMessage(actualLogMessages, deploymentDate, deploymentList.getValue().get(0));
    assertThat(actualLogMessages).contains(
        "WARN Es konnten nicht alle Deployments von allen Prozessengines abgerufen werden.");
    assertThat(actualLogMessages).contains("INFO Ende des Tasks: " + TASK_NAME);

  }

  private ProcessDeploymentList prepareDeployment(Date deploymentDate)
  {
    TreeMap<String, String> keysAndNames = new TreeMap<>();
    keysAndNames.put("processKey1", "processName1");
    keysAndNames.put("processKey2", "processName2");

    ProcessDeployment deployment1 = ProcessDeployment.builder()
        .deploymentDate(deploymentDate)
        .deploymentId("deploymentId1")
        .deploymentName("deploymentName1")
        .processDefinitionKeysAndNames(keysAndNames)
        .build();

    return ProcessDeploymentList.builder().complete(true).value(List.of(deployment1)).build();
  }

  private void prepareLogging()
  {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    listAppender = (ListAppender) config.getAppenders().get("ListAppender");
  }

  private void assertDeploymentLogMessage(List<String> actualLogMessages, Date deploymentDate,
      ProcessDeployment deployment1)
  {
    String expectedLogMessage = createExpectedLogMessage(deploymentDate, deployment1);
    assertThat(actualLogMessages).contains(expectedLogMessage);
  }

  private String createExpectedLogMessage(Date deploymentDate, ProcessDeployment deployment1)
  {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    String expectedDeploymentDateString = format.format(deploymentDate);

    return "INFO Vorhandene Deployments:\n" +
        expectedDeploymentDateString +
        " " +
        deployment1.getDeploymentName() +
        " " +
        deployment1.getDeploymentId() +
        "\n" +
        " - " +
        "processKey1" +
        " " +
        "processName1" +
        "\n" +
        " - " +
        "processKey2" +
        " " +
        "processName2" +
        "\n";
  }

  private void assertRequest(HttpHandler.Request request)
  {
    assertThat(request.getRequestMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo(ListProcessesHandler.API_PATH);
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

  private HttpHandler createAndStartHttpServer(ProcessDeploymentList deploymentList)
  {
    HttpHandler httpHandler = createHttpHandler(deploymentList);
    httpServer =
        HttpServerFactory.createAndStartHttpServer(ListProcessesHandler.API_PATH, httpHandler);

    return httpHandler;
  }

  @SneakyThrows
  private HttpHandler createHttpHandler(ProcessDeploymentList deploymentList)
  {
    return new HttpHandler(200, OBJECT_MAPPER.writeValueAsBytes(deploymentList));
  }
}
