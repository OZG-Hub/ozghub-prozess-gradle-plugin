package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.gradle.api.GradleException;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.FileHelper;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.request.DuplicateProcessKeyAction;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessDeploymentResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeployProcessHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/ozghub/deploy";

  private static final String MODEL_DIR = "/build/models";

  private static final ServerConnectionHelper<ProcessDeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(ProcessDeploymentResponse.class);

  private final File projectDir;

  private final String filePath;

  private final String deploymentName;

  private final DuplicateProcessKeyAction duplicateProcesskeyAction;

  private final String engineId;

  public DeployProcessHandler(Environment env,
      File projectDir,
      String filePath,
      String deploymentName,
      DuplicateProcessKeyAction duplicateKeyAction,
      String engineId)
  {
    super(env);
    this.projectDir = projectDir;
    this.filePath = filePath;
    this.deploymentName = deploymentName;
    this.duplicateProcesskeyAction =
        Objects.requireNonNullElse(duplicateKeyAction, DuplicateProcessKeyAction.ERROR);
    this.engineId = engineId;
  }

  public void deploy()
  {
    log.info("Start des Tasks: Deployment eines Prozessmodells");

    try
    {
      byte[] data = createDeploymentArchive();
      Map<String, String> headers = getHeaderParameters();
      ProcessDeploymentResponse response = CONNECTION_HELPER.post(environment, API_PATH, headers, data);
      logEndOfTask(response);
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }
  }

  private byte[] createDeploymentArchive()
  {
    Path folder = FileHelper.getCustomFolderOrDefault(projectDir, filePath, MODEL_DIR);

    return FileHelper.createArchiveForFilesInFolder(folder);
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.PROCESS_DEPLOYMENT_NAME, deploymentName);
    headers.put(HTTPHeaderKeys.PROCESS_DUPLICATION, duplicateProcesskeyAction.toString());
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/java-archive");

    if (engineId != null)
    {
      headers.put(HTTPHeaderKeys.PROCESS_ENGINE, engineId);
    }

    return headers;
  }

  private void logEndOfTask(ProcessDeploymentResponse response)
  {
    log.info("Das Deployment wurde erfolgreich abgeschlossen:");
    log.info("- ID des Deployments: {}", response.getDeploymentId());
    log.info("- Prozessdefinitionen mit folgenden Prozess-Keys wurden deployt:");
    response.getProcessKeys().forEach(k -> log.info("  - {}", k));

    if (DuplicateProcessKeyAction.ERROR != duplicateProcesskeyAction
        && response.getDuplicateKeys() != null
        && !response.getDuplicateKeys().isEmpty())
    {
      log.info("- Prozess-Keys, die bereits Teil eines Deployments waren:");
      response.getDuplicateKeys().forEach(k -> log.info("  - {}", k));

      if (DuplicateProcessKeyAction.UNDEPLOY == duplicateProcesskeyAction)
      {
        log.info("  ---> Diese Prozessmodelle wurden undeployt");
        log.info("- Die folgenden Prozess-Deployments wurden gelöscht:");
        response.getRemovedDeploymentIds().forEach(k -> log.info("  - {}", k));
      }

    }

    log.info("Ende des Tasks: Deployment eines Prozessmodells");
  }
}
