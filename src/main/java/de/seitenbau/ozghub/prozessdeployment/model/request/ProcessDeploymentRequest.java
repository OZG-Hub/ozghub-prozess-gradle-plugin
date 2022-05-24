package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDeploymentRequest
{
  private String barArchiveBase64;

  private String deploymentName;

  private Map<String, ProcessMetadata> metadata;
}
