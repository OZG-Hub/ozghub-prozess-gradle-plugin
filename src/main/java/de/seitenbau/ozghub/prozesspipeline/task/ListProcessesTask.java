package de.seitenbau.ozghub.prozesspipeline.task;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozesspipeline.handler.DeployFormsHandler;
import de.seitenbau.ozghub.prozesspipeline.handler.ListProcessesHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListProcessesTask extends DefaultPluginTask
{

  @TaskAction
  public void run()
  {
    ListProcessesHandler handler = new ListProcessesHandler(getEnvironment());

    handler.list();
  }
}
