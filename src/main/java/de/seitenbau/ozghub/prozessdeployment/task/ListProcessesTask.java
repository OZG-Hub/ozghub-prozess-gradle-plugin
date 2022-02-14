package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.ListProcessesHandler;
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

    handler.list(this.getName());
  }
}
