package de.seitenbau.ozghub.prozesspipeline.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import de.seitenbau.ozghub.prozesspipeline.model.response.FormDeployment;
import de.seitenbau.ozghub.prozesspipeline.model.response.FormDeploymentList;
import lombok.SneakyThrows;

public class ListFormsHandlerTest
{
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String DATE_FORMAT = "YYYY-MM-dd hh:mm:ss";
  public static final String TASK_NAME = "listForms";

  private HttpServer httpServer = null;

  private ListFormsHandler sut;

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
  public void listForms()
  {
    // arrange
    prepareLogging();

    Date deploymentDate = new Date();

    FormDeploymentList deploymentList = prepareDeployment(deploymentDate);

    HttpHandler httpHandler = createAndStartHttpServer(deploymentList);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new ListFormsHandler(env);

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
    assertDeploymentLogMessage(actualLogMessages, deploymentDate, deploymentList.getDeploymentList().get(0));
    assertThat(actualLogMessages).contains("INFO Ende des Tasks: " + TASK_NAME);
  }

  private FormDeploymentList prepareDeployment(Date deploymentDate)
  {

    FormDeployment deployment1 = FormDeployment.builder()
        .mandantId("1")
        .formName("formName")
        .formVersion("formVersion")
        .language("de")
        .deploymentDate(deploymentDate)
        .deploymentId(1L)
        .build();

    return FormDeploymentList.builder().deploymentList(List.of(deployment1)).build();
  }

  private void prepareLogging()
  {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    listAppender = (ListAppender) config.getAppenders().get("ListAppender");
  }

  private void assertDeploymentLogMessage(List<String> actualLogMessages, Date deploymentDate,
      FormDeployment deployment1)
  {
    String expectedLogMessage = createExpectedLogMessage(deploymentDate, deployment1);
    assertThat(actualLogMessages).contains(expectedLogMessage);
  }

  private String createExpectedLogMessage(Date deploymentDate, FormDeployment deployment1)
  {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    String expectedDeploymentDateString = format.format(deploymentDate);

    return "INFO Vorhandene Deployments:\n"
        + deployment1.getDeploymentName()
        + " "
        + deployment1.getLanguage()
        + " "
        + deployment1.getDeploymentId()
        + " "
        + expectedDeploymentDateString
        + "\n";
  }

  private void assertRequest(HttpHandler.Request request)
  {
    assertThat(request.getRequestMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo(ListFormsHandler.API_PATH);
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

  private HttpHandler createAndStartHttpServer(FormDeploymentList deploymentList)
  {
    HttpHandler httpHandler = createHttpHandler(deploymentList);
    httpServer =
        HttpServerFactory.createAndStartHttpServer(ListFormsHandler.API_PATH, httpHandler);

    return httpHandler;
  }

  @SneakyThrows
  private HttpHandler createHttpHandler(FormDeploymentList deploymentList)
  {
    return new HttpHandler(200, OBJECT_MAPPER.writeValueAsBytes(deploymentList));
  }
}
