package de.seitenbau.ozghub.prozessdeployment.task;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DefaultPluginTask extends DefaultTask
{
  /** URL zur OZG-Hub-Umgebung. */
  @Input
  private String url;

  /** Benutzername zur Authentifizierung. */
  @Input
  private String user;

  /** Password zur Authentifizierung. */
  @Input
  private String password;

  @Internal
  Environment getEnvironment()
  {
    return new Environment(url, user, password);
  }

  @Internal
  File getProjectDir()
  {
    return getProject().getProjectDir();
  }
}
