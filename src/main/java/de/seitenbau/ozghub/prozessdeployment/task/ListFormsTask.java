package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.ListFormsHandler;
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

    handler.list(getName());
  }
}
