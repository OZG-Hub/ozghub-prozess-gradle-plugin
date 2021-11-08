package de.seitenbau.ozghub.prozesspipeline.common;

/**
 * Keys von HTTP-Headern, welche Teil der Requests an Schnittstellen sind.
 * Änderungen hier müssen auf die Schnittstellen in den Resource-Klassen übertragen werden.
 */
public final class HTTPHeaderKeys
{
  public static final String PROCESS_DEPLOYMENT_NAME = "X-OZG-Deployment-Name";
  public static final String PROCESS_DUPLICATION = "X-OZG-Process-Duplication";
  public static final String PROCESS_ENGINE = "X-OZG-Process-Engine";

  public static final String CONTENT_TYPE = "Content-Type";
  public static final String AUTHORIZATION = "Authorization";

  private HTTPHeaderKeys()
  {
  }
}
