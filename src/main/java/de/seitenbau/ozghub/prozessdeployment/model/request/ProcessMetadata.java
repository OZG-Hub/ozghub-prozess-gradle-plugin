package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class ProcessMetadata
{
  private boolean servicekontolos;

  private Set<ProcessAuthenticationType> authenticationTypes;

  private Map<String, LevelOfAssuranceAttributeDetail> levelOfAssuranceAttributes;
}
