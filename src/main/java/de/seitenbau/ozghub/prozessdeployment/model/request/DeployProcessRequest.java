package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.util.Map;

import lombok.Data;

@Data
public class DeployProcessRequest
{
  private String barArchiveBase64;

  private String deploymentName;

  private Map<String, ProcessMetadata> metadata;
}
