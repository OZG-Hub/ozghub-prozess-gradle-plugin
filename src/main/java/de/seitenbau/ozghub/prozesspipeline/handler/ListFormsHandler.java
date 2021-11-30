package de.seitenbau.ozghub.prozesspipeline.handler;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.model.response.FormDeploymentList;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListFormsHandler extends AbstractListHandler<FormDeploymentList>
{
  public static final String API_PATH = "/formulare/ozghub/list";

  public ListFormsHandler(Environment environment)
  {
    super(environment, FormDeploymentList.class, API_PATH);
  }

  @Override
  protected void writeLogEntries(FormDeploymentList deploymentList)
  {
    log.info("Deployment-Name Sprache Deployment-Id Deployment-Datum:");
    StringBuilder sb = new StringBuilder();
    sb.append("Vorhandene Deployments:\n");
    deploymentList.getDeploymentList().forEach(
        d -> {
          sb.append(d.getDeploymentName());
          sb.append(" ");
          sb.append(d.getLanguage());
          sb.append(" ");
          sb.append(d.getDeploymentId());
          sb.append(" ");
          sb.append(formatDate(d.getDeploymentDate()));
          sb.append("\n");
        });
    log.info(sb.toString());
  }
}
