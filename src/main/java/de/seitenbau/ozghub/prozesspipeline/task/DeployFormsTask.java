package de.seitenbau.ozghub.prozesspipeline.task;

import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozesspipeline.handler.DeployFormsHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployFormsTask extends DefaultPluginTask
{

  @TaskAction
  public void run()
  {
    DeployFormsHandler handler = new DeployFormsHandler(getEnvironment(), getProjectDir());

    handler.deploy();
  }
}
