package de.seitenbau.ozghub.prozessdeployment.handler;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.model.response.FormDeploymentList;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListFormsHandler extends AbstractListHandler<FormDeploymentList>
{
  public static final String API_PATH = "/formulare/ozghub/list";

  public ListFormsHandler(Environment environment)
  {
    super(environment,
        new TypeReference<>()
        {
        },
        API_PATH);
  }

  @Override
  protected void writeLogEntries(FormDeploymentList deploymentList)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Vorhandene Deployments:\n");
    sb.append("Deployment-Datum    | Deployment-Id | Sprache | Deployment-Name\n");
    sb.append("--------------------+---------------+---------+----------------\n");
    deploymentList.getDeploymentList().forEach(
        d -> {
          sb.append(formatDate(d.getDeploymentDate()));
          sb.append(" | ");
          sb.append(StringUtils.leftPad(String.valueOf(d.getDeploymentId()), "Deployment-Id".length()));
          sb.append(" | ");
          sb.append(StringUtils.leftPad(d.getLanguage(), "Sprache".length()));
          sb.append(" | ");
          sb.append(d.getDeploymentName());
          sb.append("\n");
        });
    log.info(sb.toString());
  }
}
