package de.seitenbau.ozghub.prozessdeployment.model.request;

import lombok.Data;

@Data
public class LevelOfAssuranceAttribute
{
  private LevelOfAssurance level;

  private boolean required;
}
