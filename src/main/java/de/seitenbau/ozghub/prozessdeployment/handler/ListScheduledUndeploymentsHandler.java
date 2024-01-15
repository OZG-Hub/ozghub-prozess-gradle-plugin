package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.request.ScheduledUndeployment;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListScheduledUndeploymentsHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/scheduled/undeployment/list";

  private final ServerConnectionHelper<Aggregated<List<ScheduledUndeployment>>> serverConnectionHelper =
      new ServerConnectionHelper<>(new TypeReference<>()
      {
      });

  public ListScheduledUndeploymentsHandler(Environment environment)
  {
    super(environment);
  }

  public Aggregated<List<ScheduledUndeployment>> listScheduledUndeployments()
  {
    log.info("Starte Auflistung der zeitgesteuerten Undeployments von Online-Diensten");
    Aggregated<List<ScheduledUndeployment>> aggregatedScheduledUndeployments = getScheduledUndeployments();
    logScheduledUndeployments(aggregatedScheduledUndeployments);
    log.info("Task erfolgreich beendet.");

    return aggregatedScheduledUndeployments;
  }

  private Aggregated<List<ScheduledUndeployment>> getScheduledUndeployments()
  {
    try
    {
      return serverConnectionHelper.get(environment, API_PATH, getHeaders());
    }
    catch (IOException e)
    {
      throw new RuntimeException(
          "Fehler bei der Auflistung von zeitgesteuerten Undeployments von Online-Diensten: " +
              e.getMessage(), e);
    }
  }

  private Map<String, String> getHeaders()
  {
    return Map.of();
  }

  private void logScheduledUndeployments(
      Aggregated<List<ScheduledUndeployment>> aggregatedScheduledUndeployments)
  {
    log.info("Zeitgesteuerten Undeployments:\n " + aggregatedScheduledUndeployments); // TODO
  }
}
