package de.seitenbau.ozghub.prozesspipeline.handler;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozesspipeline.model.response.ProcessUndeploymentResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UndeployProcessHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/ozghub/undeploy";

  private static final ServerConnectionHelper<ProcessUndeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(ProcessUndeploymentResponse.class);

  private final String deploymentId;

  private final boolean deleteProcessInstances;

  public UndeployProcessHandler(Environment env, String deploymentId, boolean deleteProcessInstances)
  {
    super(env);
    this.deploymentId = deploymentId;
    this.deleteProcessInstances = deleteProcessInstances;
  }

  public void undeploy()
  {
    log.info("Start des Tasks: Löschen eines Prozess-Deployments");

    try
    {
      Map<String, String> headers = getHeaderParameters();
      ProcessUndeploymentResponse response = CONNECTION_HELPER.delete(environment, API_PATH, headers);
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
    headers.put(HTTPHeaderKeys.DELETE_PROCESS_INSTANCES, Boolean.toString(deleteProcessInstances));
    return headers;
  }

  private void logEndOfTask(ProcessUndeploymentResponse response)
  {
    log.info("Das Undeployment wurde erfolgreich abgeschlossen:");
    log.info("- Prozessdefinitionen mit folgenden Prozess-Keys wurden undeployt:");
    response.getProcessKeys().forEach(k -> log.info("  - {}", k));

    log.info("Ende des Tasks: Löschen eines Prozess-Deployments");
  }
}
