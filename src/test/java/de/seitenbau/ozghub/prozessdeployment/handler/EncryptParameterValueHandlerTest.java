package de.seitenbau.ozghub.prozessdeployment.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.gradle.api.GradleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.request.EncryptParameterValueRequest;
import de.seitenbau.ozghub.prozessdeployment.model.response.EncryptParameterValueResponse;
import lombok.SneakyThrows;

public class EncryptParameterValueHandlerTest extends HandlerTestBase
{
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String TASK_NAME = "encryptParameterValue";

  private static final String PROCESS_KEY = "m1.prozessDefinitionKey";

  @TempDir
  private File directory;

  private HttpServer httpServer = null;

  private ListAppender listAppender;

  @BeforeEach
  public void before()
  {
    prepareLogging();
  }

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
    return null;
  }

  @Test
  public void encryptParameterValue_parameterValue()
  {
    // arrange
    String parameterValue = "test value";
    EncryptParameterValueResponse encryptParameterValueResponse = createReponse();

    HttpHandler httpHandler = createAndStartHttpServer(encryptParameterValueResponse);
    Environment env = createEnvironment();

    EncryptParameterValueHandler sut = createSut(env, parameterValue, null, null, false, null);

    // act
    sut.encryptParameterValue();

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest, parameterValue);
    assertRequestHeaders(actualRequest, env);
    assertDefaultSuccessLogMessages();
    assertEncryptParameterValueLogMessage(null);
  }

  @Test
  public void encryptParameterValue_parameterValue_base64()
  {
    // arrange
    String parameterValue = "abcdef";
    EncryptParameterValueResponse encryptParameterValueResponse = createReponse();

    HttpHandler httpHandler = createAndStartHttpServer(encryptParameterValueResponse);
    Environment env = createEnvironment();

    EncryptParameterValueHandler sut = createSut(env, parameterValue, null, null, true, null);

    // act
    sut.encryptParameterValue();

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest, "YWJjZGVm");
    assertRequestHeaders(actualRequest, env);
    assertDefaultSuccessLogMessages();
    assertEncryptParameterValueLogMessage(null);
  }

  @Test
  public void encryptParameterValue_writeToOutputFile() throws IOException
  {
    // arrange
    String parameterValue = "abcdef";
    String encryptedParameterValue = "ozghub:cu:TEST_test_Test";
    File outputFile = new File(directory, "test.txt");

    EncryptParameterValueResponse encryptParameterValueResponse = createReponse();

    HttpHandler httpHandler = createAndStartHttpServer(encryptParameterValueResponse);
    Environment env = createEnvironment();

    EncryptParameterValueHandler sut =
        createSut(env, parameterValue, null, null, false, outputFile.getAbsolutePath());

    // act
    sut.encryptParameterValue();

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest, parameterValue);
    assertRequestHeaders(actualRequest, env);
    assertDefaultSuccessLogMessages();
    assertEncryptParameterValueLogMessage(outputFile);

    String fileContent = Files.readString(outputFile.toPath());
    assertThat(fileContent).isEqualTo(encryptedParameterValue);
  }

  @Test
  public void encryptParameterValue_inputFile() throws IOException
  {
    // arrange
    String parameterValue = "test\n123\näöüßÄÖÜẞ";
    File inputFile = new File(directory, "test1.txt");
    inputFile.createNewFile();
    Files.writeString(inputFile.toPath(), parameterValue);

    EncryptParameterValueResponse encryptParameterValueResponse = createReponse();

    HttpHandler httpHandler = createAndStartHttpServer(encryptParameterValueResponse);
    Environment env = createEnvironment();

    EncryptParameterValueHandler sut = createSut(env, null, inputFile.getAbsolutePath(), null, false, null);

    // act
    sut.encryptParameterValue();

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest, parameterValue);
    assertRequestHeaders(actualRequest, env);
    assertDefaultSuccessLogMessages();
    assertEncryptParameterValueLogMessage(null);
  }

  @Test
  public void encryptParameterValue_inputFile_customCharset() throws IOException
  {
    // arrange
    String parameterValue = "test\n123";
    File inputFile = new File(directory, "test2.txt");
    inputFile.createNewFile();
    Files.writeString(inputFile.toPath(), parameterValue);

    EncryptParameterValueResponse encryptParameterValueResponse = createReponse();

    HttpHandler httpHandler = createAndStartHttpServer(encryptParameterValueResponse);
    Environment env = createEnvironment();

    EncryptParameterValueHandler sut = createSut(env, null, inputFile.getPath(), "ASCII", false, null);

    // act
    sut.encryptParameterValue();

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();

    assertRequest(actualRequest, parameterValue);
    assertRequestHeaders(actualRequest, env);
    assertDefaultSuccessLogMessages();
    assertEncryptParameterValueLogMessage(null);
  }

  @Test
  public void encryptParameterValue_inputFile_base64() throws IOException
  {
    // arrange
    String parameterValue = "test\n123\näöüßÄÖÜẞ";
    File inputFile = new File(directory, "test3.txt");
    inputFile.createNewFile();
    Files.writeString(inputFile.toPath(), parameterValue);

    EncryptParameterValueResponse encryptParameterValueResponse = createReponse();

    HttpHandler httpHandler = createAndStartHttpServer(encryptParameterValueResponse);
    Environment env = createEnvironment();

    EncryptParameterValueHandler sut = createSut(env, null, inputFile.getAbsolutePath(), null, true, null);

    // act
    sut.encryptParameterValue();

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest, "dGVzdAoxMjMKw6TDtsO8w5/DhMOWw5zhup4=");
    assertRequestHeaders(actualRequest, env);
    assertDefaultSuccessLogMessages();
    assertEncryptParameterValueLogMessage(null);
  }

  @Test
  public void encryptParameterValue_exception_tooManyInputParameters()
  {
    // arrange
    Environment env = new Environment("url", "user", "password");
    EncryptParameterValueHandler sut = createSut(env, "parameterValue", "inputFile", null, false, null);

    // act
    assertThatExceptionOfType(GradleException.class)
        .isThrownBy(sut::encryptParameterValue)
        .withMessage("Fehler: Der zu verschluesselnde Parameter muss entweder als Text oder als Datei"
            + " angegeben werden. Beides ist nicht erlaubt.")
        .withRootCauseExactlyInstanceOf(IllegalArgumentException.class);

    // assert
    List<String> actualLogMessages = listAppender.getEventList();
    assertThat(actualLogMessages).containsExactly("INFO Start des Tasks: " + TASK_NAME);
  }

  @Test
  public void encryptParameterValue_exception_noInputParameter()
  {
    // arrange
    Environment env = new Environment("url", "user", "password");
    EncryptParameterValueHandler sut = createSut(env, null, null, null, false, null);

    // act
    assertThatExceptionOfType(GradleException.class)
        .isThrownBy(sut::encryptParameterValue)
        .withMessage("Fehler: Der zu verschluesselnde Parameter muss entweder als Text oder als Datei"
            + " angegeben werden.")
        .withRootCauseExactlyInstanceOf(IllegalArgumentException.class);

    // assert
    List<String> actualLogMessages = listAppender.getEventList();
    assertThat(actualLogMessages).containsExactly("INFO Start des Tasks: " + TASK_NAME);
  }

  @Test
  public void encryptParameterValue_exception_outputFileExists() throws IOException
  {
    // arrange
    String parameterValue = "abcdef";
    File outputFile = new File(directory, "test4.txt");
    outputFile.createNewFile();

    EncryptParameterValueResponse encryptParameterValueResponse = createReponse();

    HttpHandler httpHandler = createAndStartHttpServer(encryptParameterValueResponse);
    Environment env = createEnvironment();

    EncryptParameterValueHandler sut =
        createSut(env, parameterValue, null, null, false, outputFile.getAbsolutePath());

    // act
    assertThatExceptionOfType(GradleException.class)
        .isThrownBy(sut::encryptParameterValue)
        .withMessage("Fehler: Die Ausgabe-Datei (" + outputFile.getAbsolutePath() + ") existiert bereits."
            + " Der verschlüsselte Parameterwert kann nur in eine neue, noch nicht existierende Datei"
            + " geschrieben werden.");

    // assert
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertThat(httpHandler.getResponseCode()).isEqualTo(200);

    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertRequest(actualRequest, parameterValue);
    assertRequestHeaders(actualRequest, env);

    List<String> logs = listAppender.getEventList();
    assertThat(logs).containsExactly(
        "INFO Start des Tasks: " + TASK_NAME,
        "INFO Sende POST-Request an " + getUrl() + "/prozessparameter/parameter/encryptParameterValue",
        "INFO Die Verschlüsselung des Parameterwertes wurde erfolgreich abgeschlossen.");

    String fileContent = Files.readString(outputFile.toPath());
    assertThat(fileContent).isEmpty();
  }

  private void prepareLogging()
  {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    listAppender = (ListAppender) config.getAppenders().get("ListAppender");
    listAppender.getEventList().clear();
  }

  private String getUrl()
  {
    return "http://localhost:" + httpServer.getAddress().getPort();
  }

  private Environment createEnvironment()
  {
    return new Environment(getUrl(), "foo1", "bar1");
  }

  private EncryptParameterValueResponse createReponse()
  {
    return EncryptParameterValueResponse.builder()
        .encryptedParameterValue("ozghub:cu:TEST_test_Test")
        .build();
  }

  private EncryptParameterValueHandler createSut(
      Environment env,
      String parameterValue,
      String inputFile,
      String charset,
      boolean base64,
      String outputFile)
  {
    return new EncryptParameterValueHandler(env, getProjectDir(),
        PROCESS_KEY, parameterValue, inputFile, charset, base64, outputFile);
  }

  private void assertDefaultSuccessLogMessages()
  {
    List<String> logs = listAppender.getEventList();
    assertThat(logs).contains("INFO Start des Tasks: " + TASK_NAME);
    assertThat(logs).contains("INFO Sende POST-Request an " + getUrl()
        + "/prozessparameter/parameter/encryptParameterValue");
    assertThat(logs)
        .contains("INFO Die Verschlüsselung des Parameterwertes wurde erfolgreich abgeschlossen.");
    assertThat(logs).contains("INFO Ende des Tasks: " + TASK_NAME);
  }

  private void assertEncryptParameterValueLogMessage(File outputFile)
  {
    List<String> actualLogMessages = listAppender.getEventList();
    if (outputFile != null)
    {
      assertThat(actualLogMessages).contains("INFO Der verschlüsselte Parameterwert wurde in die folgende"
          + " Datei geschrieben: " + outputFile.getAbsolutePath());
    }
    else
    {
      assertThat(actualLogMessages)
          .contains("INFO Der verschlüsselte Parameterwert ist: ozghub:cu:TEST_test_Test");
    }
  }

  @SneakyThrows
  private void assertRequest(HttpHandler.Request request, String parameterValue)
  {
    assertThat(request.getRequestMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo(EncryptParameterValueHandler.API_PATH);
    assertThat(request.getQuery()).isNull();

    EncryptParameterValueRequest actualRequest =
        OBJECT_MAPPER.readValue(request.getRequestBody(), EncryptParameterValueRequest.class);
    assertThat(actualRequest.getProcessKey()).isEqualTo(PROCESS_KEY);
    assertThat(actualRequest.getParameterValue()).isEqualTo(parameterValue);
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
