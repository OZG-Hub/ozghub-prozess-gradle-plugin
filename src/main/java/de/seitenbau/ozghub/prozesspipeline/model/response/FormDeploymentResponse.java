package de.seitenbau.ozghub.prozesspipeline.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDeploymentResponse
{
  /** ID des erzeugten Deployments. */
  private String deploymentId;
}