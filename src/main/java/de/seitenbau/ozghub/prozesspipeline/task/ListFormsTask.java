package de.seitenbau.ozghub.prozesspipeline.task;

import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozesspipeline.handler.ListFormsHandler;
import de.seitenbau.ozghub.prozesspipeline.handler.ListProcessesHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListFormsTask extends DefaultPluginTask
{

  @TaskAction
  public void run()
  {
    ListFormsHandler handler = new ListFormsHandler(getEnvironment());

    handler.list(this.getName());
  }
}
