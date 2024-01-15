package de.seitenbau.ozghub.prozessdeployment.handler;

import java.util.Map;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeleteScheduledUndeploymentHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/scheduled/undeployment";

  private final ServerConnectionHelper<Void> serverConnectionHelper = new ServerConnectionHelper<>(null);

  public DeleteScheduledUndeploymentHandler(Environment environment)
  {
    super(environment);
  }

  public void deleteScheduledUndeployment(String deploymentId)
  {
    log.info("Start des Tasks: deleteScheduledUndeployment");
    try
    {
      String deploymentIdEncoded = serverConnectionHelper.encodeUrl(deploymentId);
      serverConnectionHelper.delete(environment, API_PATH + "/" + deploymentIdEncoded, getHeaders(), null);
      log.info("Das geplante Undeployment wurde erfolgreich gelöscht");
      log.info("Ende des Tasks: deleteScheduledUndeployment");
    }
    catch (Exception e)
    {
      throw new RuntimeException(
          "Fehler beim Löschen eines zeitgesteuerten Undeployments eines Online-Dienstes: " + e.getMessage(),
          e);
    }
  }

  private Map<String, String> getHeaders()
  {
    return Map.of(HTTPHeaderKeys.CONTENT_TYPE, "application/json");
  }
}
