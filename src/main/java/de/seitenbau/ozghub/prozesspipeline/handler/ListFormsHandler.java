package de.seitenbau.ozghub.prozesspipeline.handler;

import java.io.IOException;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.model.response.FormDeploymentList;

public class ListFormsHandler extends AbstractListHandler<FormDeploymentList>
{
  public static final String API_PATH = "/formulare/ozghub/list";

  public ListFormsHandler(Environment environment)
  {
    super(environment, FormDeploymentList.class, API_PATH);
  }

  @Override
  protected String getListAndGenerateLogEntry() throws IOException
  {
    FormDeploymentList list = getList();
    return generateLogEntry(list);
  }

  String generateLogEntry(FormDeploymentList deploymentList)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Vorhandene Deployments:\n");
    deploymentList.getDeployments().forEach(
        d -> {
          sb.append(formatDate(d.getDeploymentDate()));
          sb.append(" ");
          sb.append(d.getDeploymentName());
          sb.append(" ");
          sb.append(d.getLanguage());
          sb.append(" ");
          sb.append("Deployment-ID: ");
          sb.append(d.getDeploymentId());
          sb.append("\n");
        });
    return sb.toString();
  }
}
