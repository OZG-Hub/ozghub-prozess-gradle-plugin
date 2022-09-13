package de.seitenbau.ozghub.prozessdeployment.model.response;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDeployment
{
  /**
   * Die Keys und Namen der im Deployment enthaltenen Prozessmodelle
   */
  private Map<String, String> processDefinitionKeysAndNames;

  /**
   * Das Deployment Datum
   */
  private Date deploymentDate;

  /**
   * Der Name des Deployments
   */
  private String deploymentName;

  /**
   * Die Deployment-ID
   */
  private String deploymentId;

  /**
   * Der Name der Version
   */
  private String versionName;
}
