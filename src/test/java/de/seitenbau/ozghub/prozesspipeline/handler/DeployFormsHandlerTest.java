package de.seitenbau.ozghub.prozesspipeline.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozesspipeline.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozesspipeline.model.response.FormDeploymentResponse;
import lombok.SneakyThrows;

public class DeployFormsHandlerTest extends HandlerTestBase
{
  private static final File TEST_FOLDER =
      new File("src/test/resources/handler/deployFormsHandler/");

  public static final String FORM_1_FILE_NAME = "form1";

  public static final String FORM_2_FILE_NAME = "form2";

  private HttpServer httpServer = null;

  private DeployFormsHandler sut;

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
    HttpHandler httpHandler = createAndStartHttpServer(FORM_1_FILE_NAME);


    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(), null);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler, "form1");

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest, "form1");
    assertRequestBody(actualRequest.getRequestBody(), "/forms/form1.json");
    assertRequestHeaders(actualRequest, env);
  }


  @Test
  public void deploy_customPathToFolder()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(FORM_1_FILE_NAME);


    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(), "src/test/resources/handler/deployFormsHandler/forms");

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler, "form1");

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest, "form1");
    assertRequestBody(actualRequest.getRequestBody(), "/forms/form1.json");
    assertRequestHeaders(actualRequest, env);
  }


  @Test
  public void deploy_customPathToFile()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(FORM_1_FILE_NAME);


    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(),
        "src/test/resources/handler/deployFormsHandler/forms/form1.json");

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler, "form1");

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest, "form1");
    assertRequestBody(actualRequest.getRequestBody(), "/forms/form1.json");
    assertRequestHeaders(actualRequest, env);
  }

    @Test
  public void deploy_multipleFilesInFolder()
  {
    // arrange
    HttpHandler form1Handler = createAndStartHttpServer(FORM_1_FILE_NAME);
    HttpHandler form2Handler = createHttpHandler(FORM_2_FILE_NAME);
    httpServer.createContext(DeployFormsHandler.API_PATH + "/" + "form2", form2Handler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployFormsHandler(env, getProjectDir(),
        "src/test/resources/handler/deployFormsHandler/multipleForms");

    // act
    sut.deploy();

    // assert
    assertResponse(form1Handler, "form1");
    assertResponse(form2Handler, "form2");

    HttpHandler.Request actualRequestForm1 = form1Handler.getRequest();
    assertRequest(actualRequestForm1, "form1");
    assertRequestBody(actualRequestForm1.getRequestBody(), "/multipleForms/form1.json");
    assertRequestHeaders(actualRequestForm1, env);

    HttpHandler.Request actualRequestForm2 = form2Handler.getRequest();
    assertRequest(actualRequestForm2, "form2");
    assertRequestBody(actualRequestForm2.getRequestBody(), "/multipleForms/form2.json");
    assertRequestHeaders(actualRequestForm2, env);
  }

  @Test
  public void deploy_error()
  {
    // arrange
    byte[] response = "Etwas ist schiefgelaufen".getBytes(StandardCharsets.UTF_8);
    HttpHandler httpHandler = new HttpHandler(500, response);
    String apiPath = DeployFormsHandler.API_PATH + "/" + "form1";
    httpServer =
        HttpServerFactory.createAndStartHttpServer(apiPath, httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");

    sut = new DeployFormsHandler(env, getProjectDir(), null);

    // act
    assertThatThrownBy(() -> sut.deploy())
        .isExactlyInstanceOf(GradleException.class)
        .hasMessage("Fehler: HTTP-Response-Code: 500 Internal Server Error | Meldung des Servers: Etwas ist "
            + "schiefgelaufen | URL: " + url + apiPath);

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(500);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest, "form1");
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

  private void assertResponse(HttpHandler handler, String formId)
  {
    assertThat(handler.countRequests()).isEqualTo(1);
    assertThat(handler.getResponseCode()).isEqualTo(200);
    assertThat(handler.getResponseBody()).isEqualTo(createDeploymentResponse(formId));
  }

  private void assertRequest(HttpHandler.Request request, String formName)
  {
    assertThat(request.getRequestMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo(DeployFormsHandler.API_PATH + "/" + formName);
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

  private HttpHandler createAndStartHttpServer(String formId)
  {
    HttpHandler httpHandler = createHttpHandler(formId);
    httpServer =
        HttpServerFactory.createAndStartHttpServer(DeployFormsHandler.API_PATH + "/" + formId, httpHandler);

    return httpHandler;
  }

  private HttpHandler createHttpHandler(String formId)
  {
    byte[] response = createDeploymentResponse(formId);
    return new HttpHandler(200, response);
  }

  @SneakyThrows
  private byte[] createDeploymentResponse(String formId)
  {
    String deploymentId = formId.substring(formId.length() - 1);
    FormDeploymentResponse response =
        FormDeploymentResponse.builder().deploymentId(deploymentId).build();

    return OBJECT_MAPPER.writeValueAsBytes(response);
  }
}
