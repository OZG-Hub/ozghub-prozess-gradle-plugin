package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.ListScheduledUndeploymentsHandler;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ListScheduledUndeploymentsTask extends DefaultPluginTask
{
  @TaskAction
  public void listScheduledUndeployments()
  {
    ListScheduledUndeploymentsHandler handler = new ListScheduledUndeploymentsHandler(getEnvironment());

    handler.list(getName());
  }
}
