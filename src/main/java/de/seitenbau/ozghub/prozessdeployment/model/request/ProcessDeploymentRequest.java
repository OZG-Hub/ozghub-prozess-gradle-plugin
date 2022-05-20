package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.util.Map;

import lombok.Data;

@Data
public class ProcessDeploymentRequest
{
  private String barArchiveBase64;

  private String deploymentName;

  private Map<String, ProcessMetadata> metadata;
}
