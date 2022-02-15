package de.seitenbau.ozghub.prozessdeployment.integrationtest;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import lombok.SneakyThrows;

public final class HttpServerFactory
{
  private HttpServerFactory()
  {
  }

  @SneakyThrows
  private static HttpServer createAndStartHttpServer()
  {
    InetSocketAddress address = new InetSocketAddress(0);
    HttpServer httpServer = HttpServer.create(address, 0);

    httpServer.setExecutor(null);
    httpServer.start();

    return httpServer;
  }

  public static HttpServer createAndStartHttpServer(String contextPath, HttpHandler httpHandler)
  {
    HttpServer httpServer = createAndStartHttpServer();
    httpServer.createContext(contextPath, httpHandler);

    return httpServer;
  }
}
