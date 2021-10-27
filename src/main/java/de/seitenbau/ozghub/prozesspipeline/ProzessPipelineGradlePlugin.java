package de.seitenbau.ozghub.prozesspipeline;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import de.seitenbau.ozghub.prozesspipeline.task.DeployProcessModelTask;

public class ProzessPipelineGradlePlugin implements Plugin<Project>
{
  public void apply(Project project)
  {
    TaskContainer tasks = project.getTasks();

    tasks.create("deployProcessModel", DeployProcessModelTask.class, (task) -> {
    }).setDescription("Deployt die gegebenen Prozessdefinitionen auf der konfigurierten Umgebung");
  }
}
