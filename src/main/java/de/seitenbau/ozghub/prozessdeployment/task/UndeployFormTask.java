package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.UndeployFormHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UndeployFormTask extends DefaultPluginTask
{
  /** ID des Deployments. */
  @Input
  private String deploymentId;

  @TaskAction
  public void run()
  {
    UndeployFormHandler handler = new UndeployFormHandler(getEnvironment(), deploymentId);
    handler.undeploy();
  }
}
