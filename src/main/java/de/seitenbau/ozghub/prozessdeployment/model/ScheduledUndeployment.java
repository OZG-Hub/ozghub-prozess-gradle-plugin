package de.seitenbau.ozghub.prozessdeployment.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public record ScheduledUndeployment(
    String deploymentId,
    Date undeploymentDate,
    Message undeploymentAnnounceMessage,
    Message undeploymentMessage,
    UndeploymentHint hint
)
{
  public ScheduledUndeployment
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

  private static void validateUndeploymentDate(Date undeploymentDate)
  {
    if (undeploymentDate == null)
    {
      throw new IllegalArgumentException("Der Parameter 'undeploymentDate' muss gesetzt sein");
    }

    if (toLocalDate(undeploymentDate).isBefore(LocalDate.now()))
    {
      throw new IllegalArgumentException(
          "Der Wert des Parameters 'undeploymentDate' muss in der Zukunft liegen");
    }
  }

  private static LocalDate toLocalDate(Date date)
  {
    if (date == null)
    {
      return null;
    }

    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
