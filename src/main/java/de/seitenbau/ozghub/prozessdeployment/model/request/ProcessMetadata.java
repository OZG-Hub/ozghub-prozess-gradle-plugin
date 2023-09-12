package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.util.Set;

import lombok.Data;

@Data
public class ProcessMetadata
{
  private boolean servicekontolos;

  private Set<ProcessAuthenticationType> authenticationTypes;
}
