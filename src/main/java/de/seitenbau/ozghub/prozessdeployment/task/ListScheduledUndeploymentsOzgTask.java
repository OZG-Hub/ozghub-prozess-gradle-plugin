package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.ListScheduledUndeploymentsOzgHandler;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ListScheduledUndeploymentsOzgTask extends DefaultPluginTask
{
  @TaskAction
  public void listScheduledUndeploymentsOzg()
  {
    ListScheduledUndeploymentsOzgHandler handler = new ListScheduledUndeploymentsOzgHandler(getEnvironment());

    handler.list(getName());
  }
}
