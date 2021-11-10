package de.seitenbau.ozghub.prozesspipeline.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozesspipeline.handler.DeployProcessModelHandler;
import de.seitenbau.ozghub.prozesspipeline.model.request.DuplicateProcessKeyAction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployProcessModelTask extends DefaultPluginTask
{
  /**
   * Pfad zum Ordner mit Prozessmodell-Dateien. Ist kein Pfad angegeben, werden die Dateien im Order
   * build/models im aktuellen Projekt verwendet.
   */
  @Input
  @Optional
  private String files;

  /** Name des Deployments. */
  @Input
  private String deploymentName;

  /** Aktion, wenn mindestens ein Prozess-Key bereits Teil eines Deployments ist. */
  @Input
  @Optional
  private DuplicateProcessKeyAction duplicateProcesskeyAction;

  /**
   * ID der Prozess-Engine, auf welche deployt werden soll. Ist keine Engine-ID gegeben wird die
   * Standard-Prozess-Engine verwendet.
   */
  @Input
  @Optional
  private String engine;

  @TaskAction
  public void run()
  {
    DeployProcessModelHandler handler = new DeployProcessModelHandler(
        getEnvironment(),
        getProjectDir(),
        files,
        deploymentName,
        duplicateProcesskeyAction,
        engine);

    handler.deploy();
  }
}
