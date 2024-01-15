package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.request.DuplicateProcessKeyAction;
import de.seitenbau.ozghub.prozessdeployment.model.request.Message;
import de.seitenbau.ozghub.prozessdeployment.model.request.ProcessDeploymentRequest;
import de.seitenbau.ozghub.prozessdeployment.model.request.ProcessMetadata;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessDeploymentResponse;
import lombok.SneakyThrows;

public class DeployProcessHandlerTest extends BaseTestHandler
{
  private static final File TEST_FOLDER =
      new File("src/test/resources/handler/deployProcessHandler/");

  public static final Message EMPTY_MESSAGE = new Message(null, null);

  private HttpServer httpServer = null;

  private DeployProcessHandler sut;

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
  public void deploy_WithDefaultMetadata()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo1", "bar1");

    sut = new DeployProcessHandler(env,
        getProjectDir(),
        null,
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.ERROR,
        "engine1",
        null,
        EMPTY_MESSAGE);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.ERROR, "engine1");
    assertRequestBody(actualRequest.getRequestBody());
  }

  @Test
  public void deploy_with_UndeploymentMessage()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");
    Message undeploymentMessage = new Message("subject", "body");
    sut = new DeployProcessHandler(env,
        getProjectDir(),
        "build/models/example.bpmn20.xml",
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.UNDEPLOY,
        null,
        "metadata",
        undeploymentMessage);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.UNDEPLOY, null);
    assertRequestBody(actualRequest.getRequestBody(), true, true);
  }

  @Test
  public void deploy_customPathToFolder_NonExistentCustomMetadataFolder()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo2", "bar2");

    sut = new DeployProcessHandler(env,
        getProjectDir(),
        "build",
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.IGNORE,
        null,
        "path/to/non-existing/metadata",
        EMPTY_MESSAGE);

    // act
    assertThatThrownBy(() -> sut.deploy())
        .isExactlyInstanceOf(GradleException.class)
        .hasMessage(
            "Fehler: Die angegebene Quelle für Metadaten (" +
                Path.of(
                    "src/test/resources/handler/deployProcessHandler/path/to/non-existing/metadata")
                + ") konnte nicht gefunden werden");

    // assert
    assertThat(httpHandler.getRequestCount()).isEqualTo(0);
  }

  @Test
  public void deploy_customPathToFolder_NonExistentDefaultMetadataFolder()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo2", "bar2");

    sut = new DeployProcessHandler(env,
        new File(getProjectDir(), "projectWithoutMetadata"),
        "build",
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.IGNORE,
        null,
        null,
        EMPTY_MESSAGE);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.IGNORE, null);
    assertRequestBody(actualRequest.getRequestBody(), false, false);
  }

  @Test
  public void deploy_customPathToFile_CustomMetadataFolder()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");

    sut = new DeployProcessHandler(env,
        getProjectDir(),
        "build/models/example.bpmn20.xml",
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.UNDEPLOY,
        null,
        "metadata",
        EMPTY_MESSAGE);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.UNDEPLOY, null);
    assertRequestBody(actualRequest.getRequestBody());
  }

  @Test
  public void deploy_invalidMetadata_UnknownAuthType()
  {
    // arrange
    Environment env = new Environment("http://wontBeCalled", "foo3", "bar3");

    sut = new DeployProcessHandler(env,
        getProjectDir(),
        "build/models/example.bpmn20.xml",
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.UNDEPLOY,
        null,
        "metadataUnknownAuthType",
        EMPTY_MESSAGE);

    // act
    assertThatRuntimeException()
        .isThrownBy(() -> sut.deploy())
        .withMessageContaining("Fehler: Fehler beim Einlesen der Metadata-Datei")
        .withMessageContaining("Cannot deserialize value of type "
            + "`de.seitenbau.ozghub.prozessdeployment.model.request.ProcessAuthenticationType` "
            + "from String \"SERVICEKONTO\": not one of the values accepted for Enum class: [BUND_ID, MUK]");
  }

  @Test
  public void deploy_invalidMetadata_UnrecognizedProperty()
  {
    // arrange
    Environment env = new Environment("http://wontBeCalled", "foo3", "bar3");

    sut = new DeployProcessHandler(env,
        getProjectDir(),
        "build/models/example.bpmn20.xml",
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.UNDEPLOY,
        null,
        "metadataUnrecognizedProperty",
        EMPTY_MESSAGE);

    // act
    assertThatRuntimeException()
        .isThrownBy(() -> sut.deploy())
        .withMessageContaining("Fehler: Fehler beim Einlesen der Metadata-Datei")
        .withMessageContaining("Unrecognized field \"unknown\"");
  }

  @Test
  public void deploy_invalidMetadata_InvalidFormat()
  {
    // arrange
    Environment env = new Environment("http://wontBeCalled", "foo3", "bar3");

    sut = new DeployProcessHandler(env,
        getProjectDir(),
        "build/models/example.bpmn20.xml",
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.UNDEPLOY,
        null,
        "metadataInvalidFormat",
        EMPTY_MESSAGE);

    // act
    assertThatRuntimeException()
        .isThrownBy(() -> sut.deploy())
        .withMessageContaining("Fehler: Fehler beim Einlesen der Metadata-Datei");
  }

  @Test
  public void deploy_customPathToFile_CustomMetadataFile()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");

    sut = new DeployProcessHandler(env,
        getProjectDir(),
        "build/models/example.bpmn20.xml",
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.UNDEPLOY,
        null,
        "metadata/example.json",
        EMPTY_MESSAGE);

    // act
    sut.deploy();

    // assert
    assertResponse(httpHandler);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.UNDEPLOY, null);
    assertRequestBody(actualRequest.getRequestBody());
  }

  @Test
  public void deploy_error()
  {
    // arrange
    byte[] response = "Etwas ist schiefgelaufen".getBytes(StandardCharsets.UTF_8);
    HttpHandler httpHandler = new HttpHandler(500, response);
    httpServer = HttpServerFactory.createAndStartHttpServer(DeployProcessHandler.API_PATH, httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo3", "bar3");

    sut = new DeployProcessHandler(env,
        getProjectDir(),
        null,
        "deployment1",
        "v1.0",
        DuplicateProcessKeyAction.UNDEPLOY,
        null,
        null,
        EMPTY_MESSAGE);

    // act
    assertThatThrownBy(() -> sut.deploy())
        .isExactlyInstanceOf(GradleException.class)
        .hasMessage("Fehler: HTTP-Response-Code: 500 Internal Server Error | Meldung des Servers: Etwas ist "
            + "schiefgelaufen | URL: " + url + DeployProcessHandler.API_PATH);

    // assert
    assertThat(httpHandler.getRequestCount()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(500);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest);
    assertRequestHeaders(actualRequest, env, DuplicateProcessKeyAction.UNDEPLOY, null);
    assertRequestBody(actualRequest.getRequestBody());
  }

  private void assertRequestBody(byte[] data)
  {
    assertRequestBody(data, true, false);
  }

  @SneakyThrows
  private void assertRequestBody(byte[] data, boolean withMetadata, boolean withMessage)
  {
    ProcessDeploymentRequest
        actualDeployProcessRequest = OBJECT_MAPPER.readValue(data, ProcessDeploymentRequest.class);

    assertBpmnArchive(actualDeployProcessRequest);
    assertDeploymentName(actualDeployProcessRequest);
    assertVersionName(actualDeployProcessRequest);
    assertProcessMetadataInRequest(actualDeployProcessRequest, withMetadata);
    assertMessageSetInRequest(actualDeployProcessRequest, withMessage);
  }

  private void assertBpmnArchive(ProcessDeploymentRequest actualDeployProcessRequest) throws IOException
  {
    byte[] actualDeploymentArchive =
        Base64.getDecoder().decode(actualDeployProcessRequest.getBarArchiveBase64());

    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(actualDeploymentArchive)))
    {
      zis.getNextEntry();
      byte[] actualContentBytes = IOUtils.toByteArray(zis);
      String actualContent = new String(actualContentBytes);
      String expectedContent =
          Files.readString(getFileInProjectDir("/build/models/example.bpmn20.xml").toPath());

      assertThat(actualContent).isEqualTo(expectedContent);
      assertThat(zis.getNextEntry()).isNull();
    }
  }

  private static void assertDeploymentName(ProcessDeploymentRequest actualDeployProcessRequest)
  {
    assertThat(actualDeployProcessRequest.getDeploymentName()).isEqualTo("deployment1");
  }

  private static void assertVersionName(ProcessDeploymentRequest actualDeployProcessRequest)
  {
    assertThat(actualDeployProcessRequest.getVersionName()).isEqualTo("v1.0");
  }

  private void assertProcessMetadataInRequest(ProcessDeploymentRequest request, boolean requestHasMetadata)
      throws IOException
  {
    Map<String, ProcessMetadata> actualMetadata = request.getMetadata();
    if (requestHasMetadata)
    {
      assertThat(actualMetadata.entrySet()).hasSize(1);

      ProcessMetadata expectedProcessMetadata =
          OBJECT_MAPPER.readValue(getFileInProjectDir("/metadata/example.json"), ProcessMetadata.class);
      assertThat(actualMetadata.get("example")).usingRecursiveComparison().isEqualTo(expectedProcessMetadata);
      return;
    }

    assertThat(actualMetadata).isEmpty();
  }

  private static void assertMessageSetInRequest(ProcessDeploymentRequest request, boolean requestHasMessage)
  {
    Message message = request.getUndeploymentMessage();
    if (requestHasMessage)
    {
      assertThat(message.subject()).isNotEmpty();
      assertThat(message.body()).isNotEmpty();
      return;
    }

    assertThat(message.subject()).isNull();
    assertThat(message.body()).isNull();
  }

  private void assertResponse(HttpHandler handler)
  {
    assertThat(handler.getRequestCount()).isEqualTo(1);
    assertThat(handler.getResponseCode()).isEqualTo(200);
    assertThat(handler.getResponseBody()).isEqualTo(createDeploymentResponse());
  }

  private void assertRequest(HttpHandler.Request request)
  {
    assertThat(request.getRequestMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo(DeployProcessHandler.API_PATH);
    assertThat(request.getQuery()).isNull();
  }

  private void assertRequestHeaders(HttpHandler.Request request,
      Environment env,
      DuplicateProcessKeyAction action,
      String engineId)
  {
    Map<String, List<String>> headers = request.getHeaders();
    assertThat(headers).containsEntry(HTTPHeaderKeys.CONTENT_TYPE, List.of("application/json"));
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
    httpServer = HttpServerFactory.createAndStartHttpServer(DeployProcessHandler.API_PATH, httpHandler);
    return httpHandler;
  }

  @SneakyThrows
  private byte[] createDeploymentResponse()
  {
    ProcessDeploymentResponse response = ProcessDeploymentResponse.builder()
        .deploymentId("123")
        .processKeys(Set.of("key"))
        .duplicateKeys(Set.of("duplicateKey"))
        .removedDeploymentIds(Set.of("deploymentId"))
        .build();

    return OBJECT_MAPPER.writeValueAsBytes(response);
  }
}
