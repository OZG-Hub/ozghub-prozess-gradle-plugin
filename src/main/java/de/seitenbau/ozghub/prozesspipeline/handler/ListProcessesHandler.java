package de.seitenbau.ozghub.prozesspipeline.handler;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.model.response.ProcessDeploymentList;
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
    log.info("Deployment-Datum Deployment-Id Deployment-Name:\n Prozesskey Prozessname");
    StringBuilder sb = new StringBuilder();
    sb.append("Vorhandene Deployments:\n");
    deploymentList.getValue().forEach(
        d -> {
          sb.append(formatDate(d.getDeploymentDate()));
          sb.append(" ");
          sb.append(d.getDeploymentName());
          sb.append(" ");
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

}
