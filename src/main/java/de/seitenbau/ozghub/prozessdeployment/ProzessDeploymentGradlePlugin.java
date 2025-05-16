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

  private TaskContainer taskContainer;

  public void apply(Project project)
  {
    taskContainer = project.getTasks();
    createTask(
        "deployProcess",
        DeployProcessTask.class,
        "Deployt Prozessdefinitionen");

    createTask(
        "deployForms",
        DeployFormsTask.class,
        "Deployt Formulare");

    createTask(
        "undeployProcess",
        UndeployProcessTask.class,
        "Löscht ein Prozess-Deployment");

    createTask(
        "undeployForm",
        UndeployFormTask.class,
        "Löscht ein Formular-Deployment");

    createTask(
        "listProcesses",
        ListProcessesTask.class,
        "Zeigt eine Liste aller deployten Prozesse an");

    createTask(
        "listForms",
        ListFormsTask.class,
        "Zeigt eine Liste aller deployten Formulare an");

    createTask(
        "encryptParameterValue",
        EncryptParameterValueTask.class,
        "Verschlüsselt einen Prozessparameterwert");

    createTask(
        "createScheduledUndeploymentOzg",
        CreateScheduledUndeploymentOzgTask.class,
        "Erstellt ein zeitgesteuertes Undeployment eines Online-Dienstes.");

    createTask(
        "deleteScheduledUndeploymentOzg",
        DeleteScheduledUndeploymentOzgTask.class,
        "Löscht ein zeitgesteuertes Undeployment eines Online-Dienstes.");

    createTask(
        "listScheduledUndeploymentsOzg",
        ListScheduledUndeploymentsOzgTask.class,
        "Listet alle zeitgesteuerten Undeployments von Online-Diensten auf.");

    createTask(
        "getActiveProcessEnginesOzg",
        GetActiveProcessEnginesOzgTask.class,
        "Holt die Liste der aktuell zur Verfügung stehenden Prozess-Engines.");
  }

  private <K extends DefaultPluginTask> void createTask(String name, Class<K> aClass, String description)
  {
    K createdTask = taskContainer.create(name, aClass, (task) -> {
    });
    createdTask.setDescription(description);
    createdTask.setGroup(TASK_GROUP_NAME);
  }
}
