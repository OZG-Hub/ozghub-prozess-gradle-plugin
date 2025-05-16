package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.GetActiveProcessEnginesTaskOzgHandler;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetActiveProcessEnginesOzgTask extends DefaultPluginTask
{
  @TaskAction
  public void createScheduledUndeploymentOzg()
  {
    GetActiveProcessEnginesTaskOzgHandler handler = new GetActiveProcessEnginesTaskOzgHandler(getEnvironment());
    handler.getAndLogActiveProcessEngines();
  }
}
