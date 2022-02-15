package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.DeployFormsHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployFormsTask extends DefaultPluginTask
{
  /**
   * Pfad zum Ordner mit Formular-Dateien. Ist kein Pfad angegeben, werden die Dateien im Order
   * forms im aktuellen Projekt verwendet.
   */
  @Input
  @Optional
  private String files;

  @TaskAction
  public void run()
  {
    DeployFormsHandler handler = new DeployFormsHandler(getEnvironment(), getProjectDir(), files);

    handler.deploy();
  }
}
