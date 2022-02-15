package de.seitenbau.ozghub.prozessdeployment.handler;

import org.apache.commons.lang3.StringUtils;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessDeploymentList;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListProcessesHandler extends AbstractListHandler<ProcessDeploymentList>
{
  public static final String API_PATH = "/prozess/ozghub/list";

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
    StringBuilder sb = new StringBuilder();
    sb.append("Vorhandene Deployments:\n");
    sb.append("Deployment-Datum    | Deployment-Id | Deployment-Name\n");
    sb.append(" - Prozesskey Prozessname\n");
    sb.append("--------------------+---------------+----------------\n");
    deploymentList.getValue().forEach(
        deployment -> {
          sb.append(formatDate(deployment.getDeploymentDate()));
          sb.append(" | ");
          sb.append(StringUtils.leftPad(deployment.getDeploymentId(), "Deployment-Id".length()));
          sb.append(" | ");
          sb.append(deployment.getDeploymentName());
          sb.append("\n");
          deployment.getProcessDefinitionKeysAndNames().entrySet()
              .stream()
              .sorted((e1, e2) -> String.CASE_INSENSITIVE_ORDER.compare(e1.getKey(), e2.getKey()))
              .forEachOrdered(entry -> {
                sb.append(" - ");
                sb.append(entry.getKey());
                sb.append(" ");
                sb.append(entry.getValue());
                sb.append("\n");
              });
        });
    log.info(sb.toString());
  }
}
