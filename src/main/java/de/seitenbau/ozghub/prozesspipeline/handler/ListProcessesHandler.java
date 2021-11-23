package de.seitenbau.ozghub.prozesspipeline.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozesspipeline.model.response.DeploymentList;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListProcessesHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/ozghub/list";

  private static final String DATE_FORMAT = "YYYY-MM-dd hh:mm:ss";

  private static final ServerConnectionHelper<DeploymentList> CONNECTION_HELPER =
      new ServerConnectionHelper<>(DeploymentList.class);

  public ListProcessesHandler(Environment environment)
  {
    super(environment);
  }

  public void list()
  {
    log.info("Start des Tasks: Auflisten von deployten Prozessen");
    Map<String, String> headerParameters = getHeaderParameters();

    try
    {
      DeploymentList deploymentList = CONNECTION_HELPER.get(environment, API_PATH, headerParameters);

      generateOutput(deploymentList);
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }

    log.info("Ende des Tasks: Auflisten von deployten Prozessen");
  }

  private void generateOutput(DeploymentList deploymentList)
  {
    if (!Boolean.TRUE.equals(deploymentList.isComplete()))
    {
      log.warn("Es konnten nicht alle Deployments von allen Prozessengines abgerufen werden.");
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Vorhandene Deployments:\n");
    deploymentList.getValue().forEach(
        d -> {
          sb.append(formatDate(d.getDeploymentDate()));
          sb.append(" ");
          sb.append(d.getDeploymentName());
          sb.append(" ");
          sb.append("Deployment-ID: ");
          sb.append(d.getDeploymentId());
          sb.append("\n");
          d.getProcessDefinitionKeysAndNames().forEach((key, value) -> {
            sb.append(" - ");
            sb.append(key);
            sb.append(" ");
            sb.append(value);
            sb.append("\n");
          });
        });
    log.info(sb.toString());
  }

  private String formatDate(Date deploymentDate)
  {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    return format.format(deploymentDate);
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

    return headers;
  }
}
