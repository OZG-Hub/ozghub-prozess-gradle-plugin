package de.seitenbau.ozghub.prozessdeployment.common;

import org.apache.commons.lang3.StringUtils;

public record Environment(String url, String user, String password)
{
  public Environment
  {
    validateNotEmpty(url, "url");
    validateNotEmpty(user, "user");
    validateNotEmpty(password, "password");
  }

  private void validateNotEmpty(String str, String parameterName)
  {
    if (StringUtils.isBlank(str))
    {
      throw new IllegalArgumentException("Der Parameter '" + parameterName
          + "' muss gesetzt und nicht leer sein.");
    }
  }
}
