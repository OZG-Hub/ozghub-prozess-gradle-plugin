package de.seitenbau.ozghub.prozesspipeline.handler;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.helper.ServerConnectionHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UndeployFormHandler extends DefaultHandler
{
  public static final String API_PATH = "/formulare/ozghub/undeploy";

  private static final ServerConnectionHelper<?> CONNECTION_HELPER = new ServerConnectionHelper<>(null);

  private final String deploymentId;

  public UndeployFormHandler(Environment env, String deploymentId)
  {
    super(env);
    this.deploymentId = deploymentId;
  }

  public void undeploy()
  {
    log.info("Start des Tasks: Löschen eines Formular-Deployments");

    try
    {
      Map<String, String> headers = getHeaderParameters();
      CONNECTION_HELPER.delete(environment, API_PATH, headers);
      logEndOfTask();
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.DEPLOYMENT_ID, deploymentId);
    return headers;
  }

  private void logEndOfTask()
  {
    log.info("Das Undeployment wurde erfolgreich abgeschlossen");
    log.info("Ende des Tasks: Löschen eines Formular-Deployments");
  }
}
