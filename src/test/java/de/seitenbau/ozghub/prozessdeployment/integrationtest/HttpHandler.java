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

  public int getRequestCount()
  {
    return requests.size();
  }

  public Request getRequest()
  {
    return requests.get(0);
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException
  {
    try (InputStream is = httpExchange.getRequestBody())
    {
      requests.add(new Request(
          httpExchange.getRequestMethod(),
          is.readAllBytes(),
          httpExchange.getRequestHeaders(),
          httpExchange.getRequestURI().getPath(),
          httpExchange.getRequestURI().getQuery()));
    }

    httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
    httpExchange.sendResponseHeaders(responseCode, 0);

    try (OutputStream os = httpExchange.getResponseBody())
    {
      os.write(responseBody);
    }
    httpExchange.close();
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
