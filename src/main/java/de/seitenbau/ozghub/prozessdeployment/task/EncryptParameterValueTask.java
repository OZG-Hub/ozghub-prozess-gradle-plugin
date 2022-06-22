package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.EncryptParameterValueHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncryptParameterValueTask extends DefaultPluginTask
{
  /**
   * Der Prozess-Deployment-Schlüssel (Process Deployment Key)
   */
  @Input
  private String processDefinitionKey;

  /**
   * Der zu verschlüsselnde Parameterwert.
   */
  @Input
  private String parameterValue;

  @TaskAction
  public void run()
  {
    EncryptParameterValueHandler handler =
        new EncryptParameterValueHandler(getEnvironment(), processDefinitionKey, parameterValue);

    handler.encryptParameterValue();
  }
}
