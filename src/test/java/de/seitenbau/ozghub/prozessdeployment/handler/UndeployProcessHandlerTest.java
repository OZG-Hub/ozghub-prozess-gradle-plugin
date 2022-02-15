package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessUndeploymentResponse;
import lombok.SneakyThrows;

public class UndeployProcessHandlerTest extends HandlerTestBase
{
  private HttpServer httpServer = null;

  private UndeployProcessHandler sut;

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
    return null;
  }

  @Test
  public void undeploy()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new UndeployProcessHandler(env, "deploymentId1", false);

    // act
    sut.undeploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env, "deploymentId1", false);
  }

  @Test
  public void undeploy_error()
  {
    // arrange
    byte[] response = "Etwas ist schiefgelaufen".getBytes(StandardCharsets.UTF_8);
    HttpHandler httpHandler = new HttpHandler(500, response);
    httpServer = HttpServerFactory.createAndStartHttpServer(UndeployProcessHandler.API_PATH, httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");

    sut = new UndeployProcessHandler(env, "deploymentId2", true);

    // act
    assertThatThrownBy(() -> sut.undeploy())
        .isExactlyInstanceOf(GradleException.class)
        .hasMessage("Fehler: HTTP-Response-Code: 500 Internal Server Error | Meldung des Servers: Etwas ist "
            + "schiefgelaufen | URL: " + url + UndeployProcessHandler.API_PATH);

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(500);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env, "deploymentId2", true);
  }

  private void assertResponse(HttpHandler handler)
  {
    assertThat(handler.countRequests()).isEqualTo(1);
    assertThat(handler.getResponseCode()).isEqualTo(200);
    assertThat(handler.getResponseBody()).isEqualTo(createUndeploymentResponse());
  }

  private void assertRequest(HttpHandler.Request request)
  {
    assertThat(request.getRequestMethod()).isEqualTo("DELETE");
    assertThat(request.getPath()).isEqualTo(UndeployProcessHandler.API_PATH);
    assertThat(request.getQuery()).isNull();
  }

  private void assertRequestHeaders(HttpHandler.Request request,
      Environment env,
      String deploymentId,
      boolean deleteProcessInstances)
  {
    Map<String, List<String>> headers = request.getHeaders();
    assertThat(headers).containsEntry(HTTPHeaderKeys.DEPLOYMENT_ID, List.of(deploymentId));
    assertThat(headers).containsEntry(HTTPHeaderKeys.DELETE_PROCESS_INSTANCES,
        List.of(Boolean.toString(deleteProcessInstances)));

    String tmp = env.getUser() + ':' + env.getPassword();
    String auth = "Basic " + Base64.getEncoder().encodeToString(tmp.getBytes(StandardCharsets.UTF_8));
    assertThat(headers).containsEntry(HTTPHeaderKeys.AUTHORIZATION, List.of(auth));
  }

  private HttpHandler createAndStartHttpServer()
  {
    byte[] response = createUndeploymentResponse();
    HttpHandler httpHandler = new HttpHandler(200, response);
    httpServer = HttpServerFactory.createAndStartHttpServer(UndeployProcessHandler.API_PATH, httpHandler);
    return httpHandler;
  }

  @SneakyThrows
  private byte[] createUndeploymentResponse()
  {
    ProcessUndeploymentResponse response = ProcessUndeploymentResponse.builder()
        .processKeys(Set.of("key"))
        .build();

    return OBJECT_MAPPER.writeValueAsBytes(response);
  }
}
