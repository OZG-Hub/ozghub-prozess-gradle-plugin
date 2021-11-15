package de.seitenbau.ozghub.prozesspipeline.model.response;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDeploymentResponse
{
  /** ID des erzeugten Deployments. */
  private String deploymentId;

  /** Prozess-Keys von Prozessen, welche deployt wurden. */
  private Set<String> processKeys;

  /** Prozess-Keys von Prozessen, welche bereits deployt waren. */
  private Set<String> duplicateKeys;

  /** IDs von Deplyments, welche gelöscht wurden, wenn DuplicateProcessKeyAction = UNDEPLOY gewählt wurde. */
  private Set<String> removedDeploymentIds;
}
