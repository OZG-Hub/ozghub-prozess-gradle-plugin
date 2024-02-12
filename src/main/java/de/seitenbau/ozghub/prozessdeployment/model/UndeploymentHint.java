package de.seitenbau.ozghub.prozessdeployment.model;

import java.time.LocalDate;

public record UndeploymentHint(String text, LocalDate startToDisplay)
{
}
