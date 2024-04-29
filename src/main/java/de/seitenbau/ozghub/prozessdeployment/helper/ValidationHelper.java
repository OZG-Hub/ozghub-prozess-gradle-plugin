package de.seitenbau.ozghub.prozessdeployment.helper;

import org.apache.commons.lang3.StringUtils;

public final class ValidationHelper
{
  private ValidationHelper()
  {
  }

  public static void validateNotBlank(String str, String parameterName)
  {
    if (StringUtils.isBlank(str))
    {
      throw new IllegalArgumentException("Der Parameter '" + parameterName
          + "' muss gesetzt und nicht leer sein.");
    }
  }
}
