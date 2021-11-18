package de.seitenbau.ozghub.prozesspipeline.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozesspipeline.handler.UndeployProcessHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UndeployProcessTask extends DefaultPluginTask
{
  /** ID des Deployments. */
  @Input
  private String deploymentId;

  /** Ob aktive Prozessinstanzen des Deployments beendet werden sollen. */
  @Input
  @Optional
  private Boolean deleteProcessInstances;

  @TaskAction
  public void run()
  {
    UndeployProcessHandler handler = new UndeployProcessHandler(
        getEnvironment(),
        deploymentId,
        Boolean.TRUE.equals(deleteProcessInstances));

    handler.undeploy();
  }
}
