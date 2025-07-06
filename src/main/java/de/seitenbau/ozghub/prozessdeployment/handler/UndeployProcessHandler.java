package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.request.ProcessUndeploymentRequest;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessUndeploymentResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UndeployProcessHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/ozghub/undeployV2";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final ServerConnectionHelper<ProcessUndeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(new TypeReference<>()
      {
      });

  private final String deploymentId;

  private final boolean deleteProcessInstances;

  private final Message undeploymentMessage;

  public UndeployProcessHandler(
      Environment environment,
      String deploymentId,
      boolean deleteProcessInstances,
      Message undeploymentMessage)
  {
    super(environment);
    this.deploymentId = deploymentId;
    this.deleteProcessInstances = deleteProcessInstances;
    this.undeploymentMessage = undeploymentMessage;
  }

  public void undeploy()
  {
    log.info("Start des Tasks: Löschen eines Prozess-Deployments");

    try
    {
      Map<String, String> headers = getHeaderParameters();
      byte[] body = getRequestBody();
      ProcessUndeploymentResponse response = deleteRequest(headers, body);
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
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");
    return headers;
  }

  private byte[] getRequestBody() throws IOException
  {
    ProcessUndeploymentRequest undeploymentRequest =
        new ProcessUndeploymentRequest(deploymentId, deleteProcessInstances, undeploymentMessage);
    String undeploymentRequestString = OBJECT_MAPPER.writeValueAsString(undeploymentRequest);
    return undeploymentRequestString.getBytes(StandardCharsets.UTF_8);
  }

  private ProcessUndeploymentResponse deleteRequest(Map<String, String> headers, byte[] body)
      throws IOException
  {
    return CONNECTION_HELPER.delete(environment, API_PATH, headers, body);
  }

  private void logEndOfTask(ProcessUndeploymentResponse response)
  {
    log.info("Das Undeployment wurde asynchron gestartet:");
    log.info("- Prozessdefinitionen mit folgenden Prozess-Keys werden undeployt:");
    response.getProcessKeys().forEach(k -> log.info("  - {}", k));

    log.info("Ende des Tasks: Löschen eines Prozess-Deployments");
  }
}
