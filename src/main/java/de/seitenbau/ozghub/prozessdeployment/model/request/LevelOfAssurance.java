package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public enum LevelOfAssurance
{
  BASIS,
  SUBSTANZIELL,
  HOCH;

  @JsonCreator
  public static LevelOfAssurance fromString(String str) throws InvalidFormatException
  {
    for (LevelOfAssurance value : values())
    {
      if (value.name().equals(str))
      {
        return value;
      }
    }

    throw new InvalidFormatException(null, "Der String '" + str + "' ist kein gültiges Vertrauensniveau."
        + " Mögliche Werte sind " + Arrays.toString(values()), str, LevelOfAssurance.class);
  }
}
