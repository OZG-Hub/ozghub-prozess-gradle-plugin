package de.seitenbau.ozghub.prozessdeployment.handler;

import org.apache.commons.lang3.StringUtils;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessDeploymentList;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListProcessesHandler extends AbstractListHandler<ProcessDeploymentList>
{
  public static final String API_PATH = "/prozess/ozghub/list";

  private static final String DEPLOYMENT_ID_TITLE = "Deployment-Id";

  public ListProcessesHandler(Environment environment)
  {
    super(environment, ProcessDeploymentList.class, API_PATH);
  }

  protected void writeLogEntries(ProcessDeploymentList deploymentList)
  {
    if (!Boolean.TRUE.equals(deploymentList.isComplete()))
    {
      log.warn("Es konnten nicht alle Deployments von allen Prozessengines abgerufen werden.");
    }

    int deploymentIdLength = getMaxDeploymentIdLength(deploymentList);
    StringBuilder sb = new StringBuilder();
    sb.append("Vorhandene Deployments:\n")
        .append("Deployment-Datum    | ")
        .append(StringUtils.rightPad(DEPLOYMENT_ID_TITLE, deploymentIdLength))
        .append(" | Version-Name | Deployment-Name\n")
        .append(" - Prozesskey Prozessname\n")
        .append("--------------------+-")
        .append("-".repeat(deploymentIdLength))
        .append("-+--------------+----------------\n");
    deploymentList.getValue().forEach(
        deployment -> {
          String versionName = deployment.getVersionName() == null ? "" : deployment.getVersionName();
          sb.append(formatDate(deployment.getDeploymentDate()))
              .append(" | ")
              .append(StringUtils.leftPad(deployment.getDeploymentId(), deploymentIdLength))
              .append(" | ")
              .append(StringUtils.rightPad(versionName, "Version-Name".length()))
              .append(" | ")
              .append(deployment.getDeploymentName())
              .append("\n");
          deployment.getProcessDefinitionKeysAndNames().entrySet()
              .stream()
              .sorted((e1, e2) -> String.CASE_INSENSITIVE_ORDER.compare(e1.getKey(), e2.getKey()))
              .forEachOrdered(entry -> sb.append(" - ")
                  .append(entry.getKey())
                  .append(" ")
                  .append(entry.getValue())
                  .append("\n"));
        });
    log.info(sb.toString());
  }

  private int getMaxDeploymentIdLength(ProcessDeploymentList deploymentList)
  {
    return deploymentList.getValue()
        .stream()
        .mapToInt(deployment -> deployment.getDeploymentId().length())
        .max()
        .orElse(DEPLOYMENT_ID_TITLE.length());
  }
}
