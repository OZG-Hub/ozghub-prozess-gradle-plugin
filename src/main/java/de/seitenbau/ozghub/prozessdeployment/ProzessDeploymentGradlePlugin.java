package de.seitenbau.ozghub.prozessdeployment;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import de.seitenbau.ozghub.prozessdeployment.task.DeployFormsTask;
import de.seitenbau.ozghub.prozessdeployment.task.DeployProcessTask;
import de.seitenbau.ozghub.prozessdeployment.task.EncryptParameterValueTask;
import de.seitenbau.ozghub.prozessdeployment.task.ListFormsTask;
import de.seitenbau.ozghub.prozessdeployment.task.ListProcessesTask;
import de.seitenbau.ozghub.prozessdeployment.task.UndeployFormTask;
import de.seitenbau.ozghub.prozessdeployment.task.UndeployProcessTask;

public class ProzessDeploymentGradlePlugin implements Plugin<Project>
{
  public void apply(Project project)
  {
    TaskContainer tasks = project.getTasks();

    tasks.create("deployProcess", DeployProcessTask.class, (task) -> {
    }).setDescription("Deployt Prozessdefinitionen");

    tasks.create("deployForms", DeployFormsTask.class, (task) -> {
    }).setDescription("Deployt Formulare");

    tasks.create("undeployProcess", UndeployProcessTask.class, (task) -> {
    }).setDescription("Löscht ein Prozess-Deployment");

    tasks.create("undeployForm", UndeployFormTask.class, (task) -> {
    }).setDescription("Löscht ein Formular-Deployment");

    tasks.create("listProcesses", ListProcessesTask.class, (task) -> {
    }).setDescription("Zeigt eine Liste aller deployten Prozesse an");

    tasks.create("listForms", ListFormsTask.class, (task) -> {
    }).setDescription("Zeigt eine Liste aller deployten Formulare an");

    tasks.create("encryptParameterValue", EncryptParameterValueTask.class, (task) -> {
    }).setDescription("Verschlüsselt einen Prozessparameterwert");
  }
}
