package de.seitenbau.ozghub.prozessdeployment.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;

public class PlatformVersionCheckerTest
{
  private HttpServer httpServer = null;

  @AfterEach
  public void after()
  {
    if (httpServer != null)
    {
      httpServer.stop(0);
    }
  }

  @Test
  public void check_olderVersion_major()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(200, "1.205.1");
    PlatformVersion version = new PlatformVersion(204, 1);
    Environment env = createEnvironment();

    // act
    PlatformVersionChecker.check(env, version);

    // assert
    assertHttpRequest(httpHandler);
  }

  @Test
  public void check_olderVersion_minor()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(200, "1.204.2");
    PlatformVersion version = new PlatformVersion(204, 1);
    Environment env = createEnvironment();

    // act
    PlatformVersionChecker.check(env, version);

    // assert
    assertHttpRequest(httpHandler);
  }

  @Test
  public void check_sameVersion()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(200, "1.204.1");
    PlatformVersion version = new PlatformVersion(204, 1);
    Environment env = createEnvironment();

    // act
    PlatformVersionChecker.check(env, version);

    // assert
    assertHttpRequest(httpHandler);
  }

  @Test
  public void check_newerVersion_major()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(200, "1.204.1");
    PlatformVersion version = new PlatformVersion(205, 1);
    Environment env = createEnvironment();

    // act & assert
    assertThatRuntimeException()
        .isThrownBy(() -> PlatformVersionChecker.check(env, version))
        .withMessage("Der Plugin-Task ist nicht mit der Version der angesprochenen Umgebung kompatibel."
            + " Der Task benötigt mind. Version 1.205.1. Es ist aber Version 1.204.1");

    assertHttpRequest(httpHandler);
  }

  @Test
  public void check_newerVersion_minor()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(200, "1.204.1");
    PlatformVersion version = new PlatformVersion(204, 2);
    Environment env = createEnvironment();

    // act & assert
    assertThatRuntimeException()
        .isThrownBy(() -> PlatformVersionChecker.check(env, version))
        .withMessage("Der Plugin-Task ist nicht mit der Version der angesprochenen Umgebung kompatibel."
            + " Der Task benötigt mind. Version 1.204.2. Es ist aber Version 1.204.1");

    assertHttpRequest(httpHandler);
  }

  @Test
  public void check_invalidVersion()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(200, "99.0.0.develop.11664");
    PlatformVersion version = new PlatformVersion(204, 1);
    Environment env = createEnvironment();

    // act
    PlatformVersionChecker.check(env, version);

    // assert
    assertHttpRequest(httpHandler);
  }

  @Test
  public void check_exception()
  {
    // arrange
    HttpHandler httpHandler = createAndStartHttpServer(500, "");
    PlatformVersion version = new PlatformVersion(204, 1);
    Environment env = createEnvironment();

    // act & assert
    assertThatRuntimeException()
        .isThrownBy(() -> PlatformVersionChecker.check(env, version))
        .withMessageStartingWith("Fehler bei Prüfung der Version der angesprochenen Umgebung:"
            + " HTTP-Response-Code: 500 Internal Server Error | URL");

    assertHttpRequest(httpHandler);
  }

  private HttpHandler createAndStartHttpServer(int statusCode, String version)
  {
    de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler httpHandler =
        new HttpHandler(statusCode, version.getBytes(StandardCharsets.UTF_8));
    httpServer = HttpServerFactory.createAndStartHttpServer("/version", httpHandler);
    return httpHandler;
  }

  private Environment createEnvironment()
  {
    return new Environment("http://localhost:" + httpServer.getAddress().getPort(), "u", "p");
  }

  private void assertHttpRequest(HttpHandler httpHandler)
  {
    assertThat(httpHandler.getRequestCount()).isEqualTo(1);

    HttpHandler.Request request = httpHandler.getRequest();
    assertThat(request.getHeaders().get(HTTPHeaderKeys.ACCEPT).get(0)).isEqualTo("text/plain");
  }
}
