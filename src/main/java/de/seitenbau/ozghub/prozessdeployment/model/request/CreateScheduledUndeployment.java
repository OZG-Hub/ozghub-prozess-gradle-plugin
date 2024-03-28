package de.seitenbau.ozghub.prozessdeployment.model.request;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.UndeploymentHint;

public record CreateScheduledUndeployment(
    String deploymentId,
    LocalDate undeploymentDate,
    Message undeploymentAnnounceMessage,
    Message undeploymentMessage,
    UndeploymentHint hint
)
{
  public CreateScheduledUndeployment
  {
    validateDeploymentId(deploymentId);
    validateUndeploymentDate(undeploymentDate);
  }

  private static void validateDeploymentId(String deploymentId)
  {
    if (StringUtils.isBlank(deploymentId))
    {
      throw new IllegalArgumentException("Der Parameter 'deploymentId' muss gesetzt und nicht leer sein");
    }
  }

  private static void validateUndeploymentDate(LocalDate undeploymentDate)
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
