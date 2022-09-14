package de.seitenbau.ozghub.prozessdeployment.handler;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.response.FormUndeploymentResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UndeployFormHandler extends DefaultHandler
{
  public static final String API_PATH = "/formulare/ozghub/undeploy";

  private static final ServerConnectionHelper<FormUndeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(FormUndeploymentResponse.class);

  private final String deploymentId;

  public UndeployFormHandler(Environment environment, String deploymentId)
  {
    super(environment);
    this.deploymentId = deploymentId;
  }

  public void undeploy()
  {
    log.info("Start des Tasks: Löschen eines Formular-Deployments");

    try
    {
      Map<String, String> headers = getHeaderParameters();
      FormUndeploymentResponse response = CONNECTION_HELPER.delete(environment, API_PATH, headers);
      logEndOfTask(response);
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

  private void logEndOfTask(FormUndeploymentResponse response)
  {
    log.info("Das Undeployment wurde erfolgreich abgeschlossen");
    log.info("- Formular mit folgender ID wurde undeployt: {}", response.getId());
    log.info("Ende des Tasks: Löschen eines Formular-Deployments");
  }
}
