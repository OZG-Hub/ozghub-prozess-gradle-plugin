package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.GetActiveProcessEnginesOzgHandler;

public class GetActiveProcessEnginesOzgTask extends DefaultPluginTask
{
  @TaskAction
  public void createScheduledUndeploymentOzg()
  {
    GetActiveProcessEnginesOzgHandler handler =
        new GetActiveProcessEnginesOzgHandler(getEnvironment());
    handler.getAndLogActiveProcessEngines();
  }
}
