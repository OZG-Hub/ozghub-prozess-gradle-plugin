package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.UndeployProcessHandler;
import de.seitenbau.ozghub.prozessdeployment.model.request.Message;
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

  /**
   * Betreff der Nachricht die versendet wird beim Undeployment eines Prozesses.
   * Siehe SBW-28606
   */
  @Input
  @Optional
  private String undeploymentMessageSubject;

  /**
   * Inhalt der Nachricht die versendet wird beim Undeployment des Prozesses.
   * Siehe SBW-28606
   */
  @Input
  @Optional
  private String undeploymentMessageBody;

  @TaskAction
  public void undeployProcess()
  {
    UndeployProcessHandler handler = new UndeployProcessHandler(
        getEnvironment(),
        deploymentId,
        Boolean.parseBoolean(deleteProcessInstances),
        createUndeploymentMessage());

    handler.undeploy();
  }

  private Message createUndeploymentMessage()
  {
    return new Message(undeploymentMessageSubject, undeploymentMessageBody);
  }
}
