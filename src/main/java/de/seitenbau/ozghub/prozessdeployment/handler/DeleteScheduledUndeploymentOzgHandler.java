package de.seitenbau.ozghub.prozessdeployment.handler;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeleteScheduledUndeploymentOzgHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/scheduled/undeployment/%s";

  private final ServerConnectionHelper<Void> serverConnectionHelper = new ServerConnectionHelper<>(null);

  public DeleteScheduledUndeploymentOzgHandler(Environment environment)
  {
    super(environment);
  }

  public void deleteScheduledUndeployment(String deploymentId)
  {
    log.info("Start des Tasks: deleteScheduledUndeploymentOzg");
    validateNotBlank(deploymentId, "deploymentId");

    try
    {
      serverConnectionHelper.delete(environment, getApiPath(deploymentId), getHeaders(), null);
      log.info("Das geplante Undeployment wurde erfolgreich gelöscht");
      log.info("Ende des Tasks: deleteScheduledUndeploymentOzg");
    }
    catch (Exception e)
    {
      throw new RuntimeException(
          "Fehler beim Löschen eines zeitgesteuerten Undeployments eines Online-Dienstes: " + e.getMessage(),
          e);
    }
  }

  private void validateNotBlank(String value, String parameterName)
  {
    if (StringUtils.isBlank(value))
    {
      throw new RuntimeException("Der Parameter '%s' muss gesetzt sein.".formatted(parameterName));
    }
  }

  private Map<String, String> getHeaders()
  {
    return Map.of(HTTPHeaderKeys.CONTENT_TYPE, "application/json");
  }

  protected static String getApiPath(String deploymentId)
  {
    return API_PATH.formatted(ServerConnectionHelper.encodeUrl(deploymentId));
  }
}
