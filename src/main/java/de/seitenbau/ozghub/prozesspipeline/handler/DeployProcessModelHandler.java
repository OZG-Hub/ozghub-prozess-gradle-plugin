package de.seitenbau.ozghub.prozesspipeline.handler;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.gradle.api.GradleException;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.helper.FileHelper;
import de.seitenbau.ozghub.prozesspipeline.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozesspipeline.model.request.DuplicateProcessKeyAction;
import de.seitenbau.ozghub.prozesspipeline.model.response.ProcessDeploymentResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeployProcessModelHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/ozghub/deploy";

  private static final String MODEL_DIR = "/build/models";

  private static final ServerConnectionHelper<ProcessDeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(ProcessDeploymentResponse.class);

  private final File projectDir;

  private final String filePath;

  private final String deploymentName;

  private final DuplicateProcessKeyAction duplicateKeyAction;

  private final String engineId;

  public DeployProcessModelHandler(Environment env,
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
    this.duplicateKeyAction = Objects.requireNonNullElse(duplicateKeyAction, DuplicateProcessKeyAction.ERROR);
    this.engineId = engineId;
  }

  public void deploy()
  {
    log.info("Start des Tasks: Deployment eines Prozessmodells");

    Map<String, String> headers = getHeaderParameters();

    try
    {
      byte[] data = createDeploymentArchive();
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
    // Default path
    if (filePath == null)
    {
      return FileHelper.createArchiveForFilesInFolder(Paths.get(projectDir.getPath(), MODEL_DIR));
    }

    // Custom path
    File rootFile = new File(filePath);
    return FileHelper.createArchiveForFilesInFolder(rootFile.toPath());
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.PROCESS_DEPLOYMENT_NAME, deploymentName);
    headers.put(HTTPHeaderKeys.PROCESS_DUPLICATION, duplicateKeyAction.toString());
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

    if (response.getProcessKeys() != null)
    {
      log.info("- Prozessdefinitionen mit folgenden Prozess-Keys wurden deployt:");
      response.getProcessKeys().forEach(k -> log.info("  - {}", k));
    }

    if (response.getDuplicateKeys() != null)
    {
      log.info("- Prozess-Keys, die bereits Teil eines Deployments waren:");
      response.getDuplicateKeys().forEach(k -> log.info("  - {}", k));

      if (DuplicateProcessKeyAction.UNDEPLOY == duplicateKeyAction)
      {
        log.info("  ---> Diese Prozessmodelle wurden undeployt");
      }
    }

    log.info("Ende des Tasks: Deployment eines Prozessmodells");
  }
}
