package de.seitenbau.ozghub.prozesspipeline.model.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDeployment
{
  /**
   * Die Id des Mandanten, für den das Formular deployed wurde
   */
  private String mandantId;

  /**
   * Der Name des Formulars
   */
  private String formName;

  /**
   * Die Version des Formulars
   */
  private String formVersion;

  /**
   * Die Sprache des Formulars
   */
  private String language;

  /**
   * Das Deployment Datum
   */
  private Date deploymentDate;

  /**
   * Die Deployment-ID
   */
  private Long deploymentId;

  @JsonIgnore
  public String getDeploymentName()
  {
    return mandantId + ":" + formName + ":" + formVersion;
  }
}
