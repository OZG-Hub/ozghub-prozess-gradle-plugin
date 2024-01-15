package de.seitenbau.ozghub.prozessdeployment.handler;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.ScheduledUndeployment;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListScheduledUndeploymentsHandler
    extends AbstractListHandler<Aggregated<List<ScheduledUndeployment>>>
{
  public static final String API_PATH = "/prozess/scheduled/undeployment/list";

  public ListScheduledUndeploymentsHandler(Environment environment)
  {
    super(environment,
        new TypeReference<>()
        {
        },
        API_PATH
    );
  }

  @Override
  protected void writeLogEntries(Aggregated<List<ScheduledUndeployment>> aggregatedScheduledUndeployments)
  {
    if (!aggregatedScheduledUndeployments.isComplete())
    {
      log.warn("Es konnten nicht alle geplanten Undeployments von allen Prozessengines abgerufen werden.");
    }

    log.info("Es sind " + aggregatedScheduledUndeployments.getValue().size() + " geplanten Undeployments:"
        + getLogText(aggregatedScheduledUndeployments.getValue()));
  }

  private static String getLogText(List<ScheduledUndeployment> scheduledUndeployments)
  {
    return scheduledUndeployments.stream()
        .map(ListScheduledUndeploymentsHandler::getLogText)
        .collect(Collectors.joining());
  }

  private static String getLogText(ScheduledUndeployment scheduledUndeployment)
  {
    SimpleDateFormat formatter = new SimpleDateFormat("MM.dd.yyyy");

    return "\nDeploymentId: " + scheduledUndeployment.deploymentId() + "\n"
        + "Undeployment Datum: " + formatter.format(scheduledUndeployment.undeploymentDate()) + "\n"
        + "Ankündigungsnachricht:\n"
        + getLogText(scheduledUndeployment.undeploymentAnnouncementMessage())
        + "Nachricht:\n"
        + getLogText(scheduledUndeployment.undeploymentMessage());
  }

  private static String getLogText(Message message)
  {
    return " - Betreff: " + message.subject() + "\n"
        + " - Text: " + message.body() + "\n";
  }
}
