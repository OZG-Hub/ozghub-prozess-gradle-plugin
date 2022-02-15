package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.UndeployProcessHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UndeployProcessTask extends DefaultPluginTask
{
  /** ID des Deployments. */
  @Input
  private String deploymentId;

  /**
   * Ob aktive Prozessinstanzen des Deployments beendet werden sollen. Wird in zu boolean geparst.
   * Wenn <code>false</code> dürfen für das Undeployment keine Prozessinstanzen mehr existieren.
   */
  @Input
  @Optional
  private String deleteProcessInstances;

  @TaskAction
  public void run()
  {
    UndeployProcessHandler handler = new UndeployProcessHandler(
        getEnvironment(),
        deploymentId,
        Boolean.parseBoolean(deleteProcessInstances));

    handler.undeploy();
  }
}
