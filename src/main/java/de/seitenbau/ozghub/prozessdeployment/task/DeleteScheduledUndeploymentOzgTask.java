package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import de.seitenbau.ozghub.prozessdeployment.handler.DeleteScheduledUndeploymentOzgHandler;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeleteScheduledUndeploymentOzgTask extends DefaultPluginTask
{
  /**
   * Deployment-ID des Online-Dienstes, für den das zeitgesteuerte Undeployment gelöscht werden soll.
   */
  @Input
  @Option(option = "deploymentId",
      description = "Deployment-ID des Online-Dienstes"
          + ", für den das zeitgesteuerte Undeployment gelöscht werden soll")
  protected String deploymentId = null;

  @TaskAction
  public void deleteScheduledUndeploymentOzg()
  {
    DeleteScheduledUndeploymentOzgHandler handler =
        new DeleteScheduledUndeploymentOzgHandler(getEnvironment());
    handler.deleteScheduledUndeployment(deploymentId);
  }
}
