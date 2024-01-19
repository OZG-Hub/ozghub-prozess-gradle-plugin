package de.seitenbau.ozghub.prozessdeployment.common;

/**
 * Keys von HTTP-Headern, welche Teil der Requests an Schnittstellen sind.
 * Änderungen hier müssen auf die Schnittstellen in den Resource-Klassen übertragen werden.
 */
public final class HTTPHeaderKeys
{
  private HTTPHeaderKeys()
  {
  }

  public static final String PROCESS_DUPLICATION = "X-OZG-Process-Duplication";
  public static final String PROCESS_ENGINE = "X-OZG-Process-Engine";
  public static final String DEPLOYMENT_ID = "X-OZG-Deployment-ID";

  public static final String CONTENT_TYPE = "Content-Type";
  public static final String AUTHORIZATION = "Authorization";

  public static final String CACHE_CONTROL = "Cache-Control";
}
