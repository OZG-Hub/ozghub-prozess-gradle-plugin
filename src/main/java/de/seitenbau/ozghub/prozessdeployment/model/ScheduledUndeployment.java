package de.seitenbau.ozghub.prozessdeployment.model;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

public record ScheduledUndeployment(
    String deploymentId,
    LocalDate undeploymentDate,
    Message undeploymentAnnounceMessage,
    Message undeploymentMessage,
    UndeploymentHint hint
)
{
  public void validate()
  {
    validateDeploymentId();
    validateUndeploymentDate();
  }

  private void validateDeploymentId()
  {
    if (StringUtils.isBlank(deploymentId))
    {
      throw new IllegalArgumentException("Der Parameter 'deploymentId' muss gesetzt und nicht leer sein");
    }
  }

  private void validateUndeploymentDate()
  {
    if (undeploymentDate == null)
    {
      throw new IllegalArgumentException("Der Parameter 'undeploymentDate' muss gesetzt sein");
    }

    if (undeploymentDate.isBefore(LocalDate.now()))
    {
      throw new IllegalArgumentException(
          "Der Wert des Parameters 'undeploymentDate' muss in der Zukunft liegen");
    }
  }
}
