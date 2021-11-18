package de.seitenbau.ozghub.prozesspipeline.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozesspipeline.integrationtest.HttpServerFactory;
import lombok.SneakyThrows;

public class ServerConnectionHelperTest
{
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private HttpServer httpServer = null;

  private final ServerConnectionHelper<TestResponse> sut = new ServerConnectionHelper<>(TestResponse.class);

  @AfterEach
  private void after()
  {
    if (httpServer != null)
    {
      httpServer.stop(0);
    }
  }

  @Test
  @SneakyThrows
  public void get()
  {
    // arrange
    HttpHandler handler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo", "bar");
    Map<String, String> headers = Map.of("header-key", "header-value");

    TestResponse expected = createTestResponse();

    // act
    TestResponse actual = sut.get(env, "/api/pfad", headers);

    // assert
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    HttpHandler.Request actualRequest = handler.getRequest();
    assertThat(handler.countRequests()).isEqualTo(1);
    assertGetRequest(actualRequest, env, headers);
  }

  @Test
  @SneakyThrows
  public void get_badRequest()
  {
    // arrange
    byte[] response = "Es ist ein Fehler aufgetreten".getBytes(StandardCharsets.UTF_8);
    HttpHandler httpHandler = new HttpHandler(400, response);
    httpServer = HttpServerFactory.createAndStartHttpServer("/api/pfad", httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo", "bar");
    Map<String, String> headers = Map.of("header-key", "header-value");

    // act
    assertThatThrownBy(() -> sut.get(env, "/api/pfad", headers))
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessage("HTTP-Response-Code: 400 Bad Request | Meldung des Servers: Es ist ein Fehler aufgetreten"
            + " | URL: " + url + "/api/pfad");

    // assert
    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertGetRequest(actualRequest, env, headers);
  }

  @Test
  @SneakyThrows
  public void post()
  {
    // arrange
    HttpHandler handler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo", "bar");
    Map<String, String> headers = Map.of("header-key", "header-value");
    byte[] data = new byte[]{1, -2, 3, -4};

    TestResponse expected = createTestResponse();

    // act
    TestResponse actual = sut.post(env, "/api/pfad", headers, data);

    // assert
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    HttpHandler.Request actualRequest = handler.getRequest();
    assertThat(handler.countRequests()).isEqualTo(1);
    assertPostRequest(actualRequest, env, headers, data);
  }

  @Test
  @SneakyThrows
  public void post_badRequest()
  {
    // arrange
    byte[] response = "Es ist ein Fehler aufgetreten".getBytes(StandardCharsets.UTF_8);
    HttpHandler httpHandler = new HttpHandler(400, response);
    httpServer = HttpServerFactory.createAndStartHttpServer("/api/pfad", httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo", "bar");
    Map<String, String> headers = Map.of("header-key", "header-value");
    byte[] data = new byte[]{1, -2, 3, -4};

    // act
    assertThatThrownBy(() -> sut.post(env, "/api/pfad", headers, data))
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessage("HTTP-Response-Code: 400 Bad Request | Meldung des Servers: Es ist ein Fehler aufgetreten"
            + " | URL: " + url + "/api/pfad");

    // assert
    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertPostRequest(actualRequest, env, headers, data);
  }

  @Test
  @SneakyThrows
  public void delete()
  {
    // arrange
    HttpHandler handler = createAndStartHttpServer();

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo", "bar");
    Map<String, String> headers = Map.of("header-key", "header-value");

    TestResponse expected = createTestResponse();

    // act
    TestResponse actual = sut.delete(env, "/api/pfad", headers);

    // assert
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    HttpHandler.Request actualRequest = handler.getRequest();
    assertThat(handler.countRequests()).isEqualTo(1);
    assertDeleteRequest(actualRequest, env, headers);
  }

  @Test
  @SneakyThrows
  public void delete_badRequest()
  {
    // arrange
    byte[] response = "Es ist ein Fehler aufgetreten".getBytes(StandardCharsets.UTF_8);
    HttpHandler httpHandler = new HttpHandler(400, response);
    httpServer = HttpServerFactory.createAndStartHttpServer("/api/pfad", httpHandler);

    String url = "http://localhost:" + httpServer.getAddress().getPort();
    Environment env = new Environment(url, "foo", "bar");
    Map<String, String> headers = Map.of("header-key", "header-value");

    // act
    assertThatThrownBy(() -> sut.delete(env, "/api/pfad", headers))
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessage("HTTP-Response-Code: 400 Bad Request | Meldung des Servers: Es ist ein Fehler aufgetreten"
            + " | URL: " + url + "/api/pfad");

    // assert
    HttpHandler.Request actualRequest = httpHandler.getRequest();
    assertThat(httpHandler.countRequests()).isEqualTo(1);
    assertDeleteRequest(actualRequest, env, headers);
  }

  private void assertGetRequest(HttpHandler.Request request,
      Environment env,
      Map<String, String> headers)
  {
    assertThat(request.getRequestMethod()).isEqualTo("GET");
    assertThat(request.getRequestBody()).isEmpty();
    assertRequest(request, env, headers);
  }

  private void assertPostRequest(HttpHandler.Request request,
      Environment env,
      Map<String, String> headers,
      byte[] data)
  {
    assertThat(request.getRequestMethod()).isEqualTo("POST");
    assertThat(request.getRequestBody()).isEqualTo(data);
    assertRequest(request, env, headers);
  }

  private void assertDeleteRequest(HttpHandler.Request request,
      Environment env,
      Map<String, String> headers)
  {
    assertThat(request.getRequestMethod()).isEqualTo("DELETE");
    assertThat(request.getRequestBody()).isEmpty();
    assertRequest(request, env, headers);
  }

  private void assertRequest(HttpHandler.Request request, Environment env, Map<String, String> headers)
  {
    assertThat(request.getPath()).isEqualTo("/api/pfad");
    assertThat(request.getQuery()).isNull();

    for (Map.Entry<String, String> header : headers.entrySet())
    {
      assertThat(request.getHeaders()).containsEntry(header.getKey(), List.of(header.getValue()));
    }

    String tmp = env.getUser() + ':' + env.getPassword();
    String auth = "Basic " + Base64.getEncoder().encodeToString(tmp.getBytes(StandardCharsets.UTF_8));
    assertThat(request.getHeaders()).containsEntry(HTTPHeaderKeys.AUTHORIZATION, List.of(auth));
  }

  @SneakyThrows
  private HttpHandler createAndStartHttpServer()
  {
    byte[] response = OBJECT_MAPPER.writeValueAsBytes(createTestResponse());
    HttpHandler httpHandler = new HttpHandler(200, response);
    httpServer = HttpServerFactory.createAndStartHttpServer("/api/pfad", httpHandler);
    return httpHandler;
  }

  private TestResponse createTestResponse()
  {
    return TestResponse.builder()
        .intValue(123)
        .doubleValue(4.56)
        .strValue("abc")
        .listValue(List.of("x", "y", "z"))
        .setValue(Set.of(1, 2, 3))
        .build();
  }
}
