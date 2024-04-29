package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public enum ProcessAuthenticationType
{
  // SERVICEKONTO, is not available on OZGHub
  BUND_ID,
  MUK;

  @JsonCreator
  public static ProcessAuthenticationType fromString(String str) throws InvalidFormatException
  {
    for (ProcessAuthenticationType value : values())
    {
      if (value.name().equals(str))
      {
        return value;
      }
    }

    throw new InvalidFormatException(null, "Der String '" + str
        + "' ist keine gültige Authentisierungsoption. Mögliche Werte sind "
        + Arrays.toString(values()), str, ProcessAuthenticationType.class);
  }
}
