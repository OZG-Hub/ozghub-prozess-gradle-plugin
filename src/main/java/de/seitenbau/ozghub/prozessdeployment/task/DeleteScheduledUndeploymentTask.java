package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import de.seitenbau.ozghub.prozessdeployment.handler.DeleteScheduledUndeploymentHandler;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeleteScheduledUndeploymentTask extends DefaultPluginTask
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
  public void deleteScheduledUndeployment()
  {
    DeleteScheduledUndeploymentHandler handler = new DeleteScheduledUndeploymentHandler(getEnvironment());
    handler.deleteScheduledUndeployment(deploymentId);
  }
}
