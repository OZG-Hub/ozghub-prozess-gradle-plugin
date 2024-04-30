package de.seitenbau.ozghub.prozessdeployment.common;

import de.seitenbau.ozghub.prozessdeployment.helper.ValidationHelper;

public record Environment(String url, String user, String password)
{
  public Environment
  {
    ValidationHelper.validateNotBlank(url, "url");
    ValidationHelper.validateNotBlank(user, "user");
    ValidationHelper.validateNotBlank(password, "password");
  }
}
