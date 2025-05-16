package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessEngine;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GetActiveProcessEnginesOzgHandler extends DefaultHandler
{
  private static final String URL = "/prozess/engine/aggregated";

  private static final ServerConnectionHelper<Aggregated<List<ProcessEngine>>> CONNECTION_HELPER =
      new ServerConnectionHelper<>(new TypeReference<>()
      {
      });

  public GetActiveProcessEnginesOzgHandler(Environment environment)
  {
    super(environment);
  }

  public void getAndLogActiveProcessEngines()
  {
    log.info("Starte Auslesen der Prozess-Engines");
    List<ProcessEngine> engines = getActiveProcessEngines();
    logActiveProcessEngines(engines);
    log.info("Task erfolgreich beendet.");
  }

  private void logActiveProcessEngines(List<ProcessEngine> engines)
  {
    if (engines == null || engines.isEmpty())
    {
      log.info("Es stehen keine Prozess-Engines zur Verfügung");
      return;
    }

    if (engines.size() == 1)
    {
      log.info("Die folgende eine Prozess-Engine steht zur Verfügung:");
    }
    else
    {
      log.info("Die folgenden {} Prozess-Engines stehen zur Verfügung:", engines.size());
    }

    for (ProcessEngine engine : engines)
    {
      log.info("- '{}' mit der ID {}", engine.getName(), engine.getId());
    }
  }

  private List<ProcessEngine> getActiveProcessEngines()
  {
    try
    {
      Map<String, String> headers = getHeadersForRequest();
      Aggregated<List<ProcessEngine>> aggregatedProcessEngines = getEngines(headers);
      return aggregatedProcessEngines.getValue();
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fehler beim Laden der Prozess-Engines: " + e.getMessage(), e);
    }
  }

  private Map<String, String> getHeadersForRequest()
  {
    return Map.of(
        HTTPHeaderKeys.CONTENT_TYPE, "application/json",
        HTTPHeaderKeys.ACCEPT, "application/json");
  }

  private Aggregated<List<ProcessEngine>> getEngines(Map<String, String> headers) throws IOException
  {
    return CONNECTION_HELPER.get(environment, URL, headers);
  }
}
