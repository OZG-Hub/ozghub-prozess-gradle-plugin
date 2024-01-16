package de.seitenbau.ozghub.prozessdeployment.task;

import java.util.Date;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import de.seitenbau.ozghub.prozessdeployment.handler.CreateScheduledUndeploymentHandler;
import de.seitenbau.ozghub.prozessdeployment.model.Message;
import de.seitenbau.ozghub.prozessdeployment.model.ScheduledUndeployment;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateScheduledUndeploymentTask extends DefaultPluginTask
{
  /**
   * Deployment-ID des Online-Dienstes, der undeployt werden soll.
   */
  @Input
  @Option(option = "deploymentId",
      description = "Deployment-ID des Online-Dienstes, der undeployt werden soll")
  protected String deploymentId = null;

  /**
   * Das Datum, an dem der Online-Dienst undeployt werden soll.
   */
  @Input
  @Option(option = "undeploymentDate",
      description = "Das Datum, an dem der Online-Dienst undeployt werden soll")
  protected Date undeploymentDate = null;

  @Input
  @org.gradle.api.tasks.Optional
  @Option(option = "undeploymentAnnounceMessageSubject",
      description = "Betreff der Ankündigungsnachricht eines Undeployments")
  protected String undeploymentAnnounceMessageSubject = null;

  @Input
  @org.gradle.api.tasks.Optional
  @Option(option = "undeploymentAnnounceMessageBody",
      description = "Text der Ankündigungsnachricht eines Undeployments")
  protected String undeploymentAnnounceMessageBody = null;

  @Input
  @org.gradle.api.tasks.Optional
  @Option(option = "undeploymentMessageSubject",
      description = "Betreff der Nachricht eines Undeployments")
  protected String undeploymentMessageSubject = null;

  @Input
  @org.gradle.api.tasks.Optional
  @Option(option = "undeploymentMessageBody",
      description = "Text der Nachricht eines Undeployments")
  protected String undeploymentMessageBody = null;

  @TaskAction
  public void createScheduledUndeployment()
  {
    CreateScheduledUndeploymentHandler handler = new CreateScheduledUndeploymentHandler(getEnvironment());

    handler.createScheduledUndeployment(new ScheduledUndeployment(
            deploymentId,
            undeploymentDate,
            new Message(undeploymentAnnounceMessageSubject, undeploymentAnnounceMessageBody),
            new Message(undeploymentMessageSubject, undeploymentMessageBody)
        )
    );
  }
}
