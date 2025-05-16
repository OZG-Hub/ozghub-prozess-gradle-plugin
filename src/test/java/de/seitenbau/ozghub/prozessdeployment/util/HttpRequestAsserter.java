package de.seitenbau.ozghub.prozessdeployment.util;

import static de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys.ACCEPT;
import static de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys.AUTHORIZATION;
import static de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys.CONTENT_TYPE;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import lombok.AllArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;

@AllArgsConstructor
public class HttpRequestAsserter
{
  private final HttpHandler.Request request;

  public void assertAuthHeaderIsSet()
  {
    assertHeaderContains(AUTHORIZATION, "Basic");
  }

  public void assertHeaderContains(String headerName, String expectedValue)
  {
    assertThat(getHeaderParam(request, headerName)).contains(expectedValue);
  }

  public void assertContentTypeEquals(String expectedValue)
  {
    assertHeaderEquals(CONTENT_TYPE, expectedValue);
  }

  public void assertAcceptEquals(String expectedValue)
  {
    assertHeaderEquals(ACCEPT, expectedValue);
  }

  public void assertHeaderEquals(String headerName, String expectedValue)
  {
    assertThat(getHeaderParam(request, headerName)).isEqualTo(expectedValue);
  }

  public void assertHeaderIsNull(String headerName)
  {
    assertThat(request.getHeaders().get(headerName)).isNull();
  }

  public static String getHeaderParam(HttpHandler.Request request, String headerName)
  {
    return request.getHeaders().get(headerName).get(0);
  }
}
