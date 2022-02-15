package de.seitenbau.ozghub.prozessdeployment.integrationtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class HttpHandler implements com.sun.net.httpserver.HttpHandler
{
  private final List<Request> requests = new ArrayList<>();

  private final int responseCode;

  private final byte[] responseBody;

  public HttpHandler(int responseCode, byte[] responseBody)
  {
    this.responseCode = responseCode;
    this.responseBody = responseBody;
  }

  public int countRequests()
  {
    return requests.size();
  }

  public Request getRequest()
  {
    return getRequest(0);
  }

  public Request getRequest(int i)
  {
    return getRequests().get(i);
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException
  {
    try (InputStream is = exchange.getRequestBody())
    {
      requests.add(new Request(
          exchange.getRequestMethod(),
          is.readAllBytes(),
          exchange.getRequestHeaders(),
          exchange.getRequestURI().getPath(),
          exchange.getRequestURI().getQuery()));
    }

    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(responseCode, 0);

    try (OutputStream os = exchange.getResponseBody())
    {
      os.write(responseBody);
    }
    exchange.close();
  }

  @Getter
  @AllArgsConstructor
  public static final class Request
  {
    private final String requestMethod;

    private final byte[] requestBody;

    private final Map<String, List<String>> headers;

    private final String path;

    private final String query;
  }
}
