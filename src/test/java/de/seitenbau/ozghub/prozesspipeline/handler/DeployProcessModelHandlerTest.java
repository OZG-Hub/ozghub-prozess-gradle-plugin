package de.seitenbau.ozghub.prozesspipeline.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozesspipeline.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozesspipeline.model.request.DuplicateProcessKeyAction;
import de.seitenbau.ozghub.prozesspipeline.model.response.ProcessDeploymentResponse;
import lombok.SneakyThrows;

public class DeployProcessModelHandlerTest extends HandlerTestBase
{
  private static final File TEST_FOLDER =
      new File("src/test/resources/handler/deployProcessModelHandler/");

  private HttpServer httpServer = null;

  private DeployProcessModelHandler sut;

  @AfterEach
  private void after()
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

    sut = new DeployProcessModelHandler(env,
        getProjectDir(),
        null,
        "deployment1",
        DuplicateProcessKeyAction.ERROR,
        "engine1");

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestBody(actualRequest.getRequestBody());
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.ERROR, "engine1");
  }

  @Test
  public void deploy_customPathToFolder()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo2", "bar2");

    sut = new DeployProcessModelHandler(env,
        getProjectDir(),
        "src/test/resources/handler/deployProcessModelHandler/build",
        "deployment1",
        DuplicateProcessKeyAction.IGNORE,
        null);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestBody(actualRequest.getRequestBody());
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.IGNORE, null);
  }

  @Test
  public void deploy_customPathToFile()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");

    sut = new DeployProcessModelHandler(env,
        getProjectDir(),
        "src/test/resources/handler/deployProcessModelHandler/build/models/example.bpmn",
        "deployment1",
        DuplicateProcessKeyAction.UNDEPLOY,
        null);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestBody(actualRequest.getRequestBody());
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.UNDEPLOY, null);
  }

  @Test
  public void deploy_error()
  {
    // arrange
    byte[] response = "Etwas ist schiefgelaufen".getBytes(StandardCharsets.UTF_8);
    HttpHandler httpHandler = new HttpHandler(500, response);
    httpServer = HttpServerFactory.createAndStartHttpServer(DeployProcessModelHandler.API_PATH, httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");

    sut = new DeployProcessModelHandler(env,
        getProjectDir(),
        null,
        "deployment1",
        DuplicateProcessKeyAction.UNDEPLOY,
        null);

    // act
    assertThatThrownBy(() -> sut.deploy())
        .isExactlyInstanceOf(GradleException.class)
        .hasMessage("Fehler: HTTP-Response-Code: 500 Internal Server Error | Meldung des Servers: Etwas ist "
            + "schiefgelaufen | URL: " + url + DeployProcessModelHandler.API_PATH);

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(500);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestBody(actualRequest.getRequestBody());
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.UNDEPLOY, null);
  }

  @SneakyThrows
  private void assertRequestBody(byte[] data)
  {
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data)))
    {
      zis.getNextEntry();
      byte[] actualContentBytes = IOUtils.toByteArray(zis);
      String actualContent = new String(actualContentBytes);
      String expectedContent = Files.readString(getFileInProjectDir("/build/models/example.bpmn").toPath());

      assertThat(actualContent).isEqualTo(expectedContent);
      assertThat(zis.getNextEntry()).isNull();
    }
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
    assertThat(request.getPath()).isEqualTo(DeployProcessModelHandler.API_PATH);
    assertThat(request.getQuery()).isNull();
  }

  private void assertRequestHeaders(HttpHandler.Request request,
      Environment env,
      DuplicateProcessKeyAction action,
      String engineId)
  {
    Map<String, List<String>> headers = request.getHeaders();
    assertThat(headers).containsEntry(HTTPHeaderKeys.CONTENT_TYPE, List.of("application/java-archive"));
    assertThat(headers).containsEntry(HTTPHeaderKeys.PROCESS_DEPLOYMENT_NAME, List.of("deployment1"));
    assertThat(headers).containsEntry(HTTPHeaderKeys.PROCESS_DUPLICATION, List.of(action.toString()));

    if (engineId != null)
    {
      assertThat(headers).containsEntry(HTTPHeaderKeys.PROCESS_ENGINE, List.of(engineId));
    }

    String tmp = env.getUser() + ':' + env.getPassword();
    String auth = "Basic " + Base64.getEncoder().encodeToString(tmp.getBytes(StandardCharsets.UTF_8));
    assertThat(headers).containsEntry(HTTPHeaderKeys.AUTHORIZATION, List.of(auth));
  }

  private HttpHandler createAndStartHttpServer()
  {
    byte[] response = createDeploymentResponse();
    HttpHandler httpHandler = new HttpHandler(200, response);
    httpServer = HttpServerFactory.createAndStartHttpServer(DeployProcessModelHandler.API_PATH, httpHandler);
    return httpHandler;
  }

  @SneakyThrows
  private byte[] createDeploymentResponse()
  {
    ProcessDeploymentResponse response = ProcessDeploymentResponse.builder()
        .deploymentId("123")
        .processKeys(Set.of("key"))
        .duplicateKeys(Set.of("duplicateKey"))
        .build();

    return OBJECT_MAPPER.writeValueAsBytes(response);
  }
}