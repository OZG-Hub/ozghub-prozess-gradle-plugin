package de.seitenbau.ozghub.prozesspipeline.model.response;

import java.util.Date;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deployment
{
  /**
   * Die Keys der im Deployment enthaltenenen Prozessmodelle
   */
  private Set<String> processKeys;

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
}
