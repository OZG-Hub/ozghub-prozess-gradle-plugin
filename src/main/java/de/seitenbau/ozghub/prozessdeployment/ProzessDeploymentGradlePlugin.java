package de.seitenbau.ozghub.prozessdeployment;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import de.seitenbau.ozghub.prozessdeployment.task.CreateScheduledUndeploymentOzgTask;
import de.seitenbau.ozghub.prozessdeployment.task.DefaultPluginTask;
import de.seitenbau.ozghub.prozessdeployment.task.DeleteScheduledUndeploymentOzgTask;
import de.seitenbau.ozghub.prozessdeployment.task.DeployFormsTask;
import de.seitenbau.ozghub.prozessdeployment.task.DeployProcessTask;
import de.seitenbau.ozghub.prozessdeployment.task.EncryptParameterValueTask;
import de.seitenbau.ozghub.prozessdeployment.task.GetActiveProcessEnginesOzgTask;
import de.seitenbau.ozghub.prozessdeployment.task.ListFormsTask;
import de.seitenbau.ozghub.prozessdeployment.task.ListProcessesTask;
import de.seitenbau.ozghub.prozessdeployment.task.ListScheduledUndeploymentsOzgTask;
import de.seitenbau.ozghub.prozessdeployment.task.UndeployFormTask;
import de.seitenbau.ozghub.prozessdeployment.task.UndeployProcessTask;

public class ProzessDeploymentGradlePlugin implements Plugin<Project>
{
  private static final String TASK_GROUP_NAME = "OZG-Hub";

  public void apply(Project project)
  {
    TaskContainer tasks = project.getTasks();
    createTask(
        tasks,
        "deployProcess",
        DeployProcessTask.class,
        "Deployt Prozessdefinitionen");

    createTask(
        tasks,
        "deployForms",
        DeployFormsTask.class,
        "Deployt Formulare");

    createTask(
        tasks,
        "undeployProcess",
        UndeployProcessTask.class,
        "Löscht ein Prozess-Deployment");

    createTask(
        tasks,
        "undeployForm",
        UndeployFormTask.class,
        "Löscht ein Formular-Deployment");

    createTask(
        tasks,
        "listProcesses",
        ListProcessesTask.class,
        "Zeigt eine Liste aller deployten Prozesse an");

    createTask(
        tasks,
        "listForms",
        ListFormsTask.class,
        "Zeigt eine Liste aller deployten Formulare an");

    createTask(
        tasks,
        "encryptParameterValue",
        EncryptParameterValueTask.class,
        "Verschlüsselt einen Prozessparameterwert");

    createTask(
        tasks,
        "createScheduledUndeploymentOzg",
        CreateScheduledUndeploymentOzgTask.class,
        "Erstellt ein zeitgesteuertes Undeployment eines Online-Dienstes.");

    createTask(
        tasks,
        "deleteScheduledUndeploymentOzg",
        DeleteScheduledUndeploymentOzgTask.class,
        "Löscht ein zeitgesteuertes Undeployment eines Online-Dienstes.");

    createTask(
        tasks,
        "listScheduledUndeploymentsOzg",
        ListScheduledUndeploymentsOzgTask.class,
        "Listet alle zeitgesteuerten Undeployments von Online-Diensten auf.");

    createTask(
        tasks,
        "getActiveProcessEnginesOzg",
        GetActiveProcessEnginesOzgTask.class,
        "Holt die Liste der aktuell zur Verfügung stehenden Prozess-Engines.");
  }

  private <K extends DefaultPluginTask> void createTask(TaskContainer tasks, String name, Class<K> taskClass,
      String description)
  {
    tasks.register(name, taskClass, task -> {
      task.setGroup(TASK_GROUP_NAME);
      task.setDescription(description);
    });
  }
}
