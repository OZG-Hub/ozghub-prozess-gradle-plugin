package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessEngine;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GetActiveProcessEnginesOzgHandler extends AbstractListHandler<Aggregated<List<ProcessEngine>>>
{
  private static final String API_PATH = "/prozess/engine/aggregated";

  public GetActiveProcessEnginesOzgHandler(Environment environment)
  {
    super(
        environment,
        new TypeReference<>()
        {
        }, API_PATH);
  }

  @Override
  protected void writeLogEntries(Aggregated<List<ProcessEngine>> aggregatedList) throws IOException
  {
    if (!aggregatedList.isComplete())
    {
      log.info("Nicht alle ProcessEngines waren erreichbar.");
    }

    logActiveProcessEngines(aggregatedList.getValue());
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

  @Override
  protected Map<String, String> getHeaderParameters()
  {
    return Map.of(
        HTTPHeaderKeys.CONTENT_TYPE, "application/json",
        HTTPHeaderKeys.ACCEPT, "application/json");
  }
}
