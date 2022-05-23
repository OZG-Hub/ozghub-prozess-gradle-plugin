package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.DeployProcessHandler;
import de.seitenbau.ozghub.prozessdeployment.model.request.DuplicateProcessKeyAction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployProcessTask extends DefaultPluginTask
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

    /**
   * Pfad zum Ordner mit Metadaten-Dateien. Ist kein Pfad angegeben, werden die Dateien im Order
   * metadata im aktuellen Projekt verwendet.
   */
  @Input
  @Optional
  private String metadataFolder;

  @TaskAction
  public void run()
  {
    DeployProcessHandler handler = new DeployProcessHandler(
        getEnvironment(),
        getProjectDir(),
        files,
        deploymentName,
        duplicateProcesskeyAction,
        engine,
        metadataFolder);

    handler.deploy();
  }
}
