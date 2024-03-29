package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.util.Map;

import de.seitenbau.ozghub.prozessdeployment.model.Message;
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

  private String versionName;

  private Map<String, ProcessMetadata> metadata;

  private Message undeploymentMessage;
}
