package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.response.FormDeploymentResponse;
import lombok.SneakyThrows;

public class DeployFormsHandlerTest extends HandlerTestBase
{
  private static final File TEST_FOLDER =
      new File("src/test/resources/handler/deployFormsHandler/");

  private HttpServer httpServer = null;

  private DeployFormsHandler sut;

  @AfterEach
  public void after()
  {
    if (httpServer != null)
    {
      httpServer.stop(0);
    }
  }

  @Override
  protected File getTestFolder()
  {
    return TEST_FOLDER;
  }

  @Test
  public void deploy()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(), null);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest);
    assertRequestBody(actualRequest.getRequestBody(), "/forms/form1.json");
    assertRequestHeaders(actualRequest, env);
  }

  @Test
  public void deploy_customPathToFolder()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(), "forms");

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest);
    assertRequestBody(actualRequest.getRequestBody(), "/forms/form1.json");
    assertRequestHeaders(actualRequest, env);
  }

  @Test
  public void deploy_customPathToFile()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(), "forms/form1.json");

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest);
    assertRequestBody(actualRequest.getRequestBody(), "/forms/form1.json");
    assertRequestHeaders(actualRequest, env);
  }

  @Test
  public void deploy_customPathToFile_notFound()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();
    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(), "forms/form2.json");

    // act
    assertThatExceptionOfType(GradleException.class)
        .isThrownBy(() -> sut.deploy())
        .withMessage("Fehler: Fehler beim Lesen der Dateien in Ordner "
            + Path.of("src/test/resources/handler/deployFormsHandler/forms/form2.json"));

    // assert
    assertThat(httpHandler.countRequests()).isZero();
  }

  @Test
  public void deploy_multipleFilesInFolder() throws IOException
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(), "multipleForms");

    // act
    sut.deploy();

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(2);

    List<HttpHandler.Request> actualRequests = httpHandler.getRequests();
    actualRequests.forEach(r -> assertRequestHeaders(r, env));
    actualRequests.forEach(this::assertRequest);

    assertRequestBodies(actualRequests);
  }

  private void assertRequestBodies(List<HttpHandler.Request> actualRequests) throws IOException
  {
    List<String> actualRequestBodyContents =
        actualRequests.stream().map(r -> new String(r.getRequestBody(), StandardCharsets.UTF_8)).collect(
            Collectors.toList());

    List<String> expectedRequestBodyContents = new ArrayList<>();
    expectedRequestBodyContents.add(
        Files.readString(getFileInProjectDir("/multipleForms/form1.json").toPath()));
    expectedRequestBodyContents.add(
        Files.readString(getFileInProjectDir("/multipleForms/form2.json").toPath()));

    assertThat(actualRequestBodyContents).containsExactlyInAnyOrderElementsOf(expectedRequestBodyContents);
  }

  @Test
  public void deploy_error()
  {
    // arrange
    byte[] response = "Etwas ist schiefgelaufen".getBytes(StandardCharsets.UTF_8);
    HttpHandler httpHandler = new HttpHandler(500, response);
    httpServer =
        HttpServerFactory.createAndStartHttpServer(DeployFormsHandler.API_PATH, httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");

    sut = new DeployFormsHandler(env, getProjectDir(), null);

    // act
    assertThatThrownBy(() -> sut.deploy())
        .isExactlyInstanceOf(GradleException.class)
        .hasMessage("Fehler: HTTP-Response-Code: 500 Internal Server Error | Meldung des Servers: Etwas ist "
            + "schiefgelaufen | URL: " + url + DeployFormsHandler.API_PATH);

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(500);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest);
    assertRequestBody(actualRequest.getRequestBody(), "/forms/form1.json");
    assertRequestHeaders(actualRequest, env);
  }

  @SneakyThrows
  private void assertRequestBody(byte[] data, String filePathInProjectDir)
  {
    String expectedContent = Files.readString(getFileInProjectDir(filePathInProjectDir).toPath());
    String actualContent = new String(data, StandardCharsets.UTF_8);

    assertThat(actualContent).isEqualTo(expectedContent);

  }

  private void assertResponse(HttpHandler handler)
  {
    assertThat(handler.countRequests()).isEqualTo(1);
    assertThat(handler.getResponseCode()).isEqualTo(200);
    assertThat(handler.getResponseBody()).isEqualTo(createDeploymentResponse());
  }

  private void assertRequest(HttpHandler.Request request)
  {
    assertThat(request.getRequestMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo(DeployFormsHandler.API_PATH);
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

  private HttpHandler createAndStartHttpServer()
  {
    HttpHandler httpHandler = createHttpHandler();
    httpServer =
        HttpServerFactory.createAndStartHttpServer(DeployFormsHandler.API_PATH, httpHandler);

    return httpHandler;
  }

  private HttpHandler createHttpHandler()
  {
    byte[] response = createDeploymentResponse();
    return new HttpHandler(200, response);
  }

  @SneakyThrows
  private byte[] createDeploymentResponse()
  {
    FormDeploymentResponse response =
        FormDeploymentResponse.builder().deploymentId("123").build();

    return OBJECT_MAPPER.writeValueAsBytes(response);
  }
}
