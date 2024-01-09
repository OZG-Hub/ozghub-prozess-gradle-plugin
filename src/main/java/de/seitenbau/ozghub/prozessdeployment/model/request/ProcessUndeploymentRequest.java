package de.seitenbau.ozghub.prozessdeployment.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessUndeploymentRequest
{
  private String deploymentId;
  private boolean deleteProcessInstances;
  private Message undeploymentMessage;
}
