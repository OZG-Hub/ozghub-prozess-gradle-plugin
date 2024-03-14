package de.seitenbau.ozghub.prozessdeployment.model.response;

import java.time.LocalDate;

import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.UndeploymentHint;

public record ScheduledUndeployment(
    String deploymentId,
    LocalDate undeploymentDate,
    Message undeploymentAnnounceMessage,
    Message undeploymentMessage,
    UndeploymentHint hint)
{
}
