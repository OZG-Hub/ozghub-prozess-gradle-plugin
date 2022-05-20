package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.GradleException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.FileHelper;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.request.DuplicateProcessKeyAction;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessDeploymentResponse;
import de.seitenbau.ozghub.prozessdeployment.model.request.DeployProcessRequest;
import de.seitenbau.ozghub.prozessdeployment.model.request.ProcessMetadata;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeployProcessHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/ozghub/deployWithMetadata";

  private static final String MODEL_DIR = "/build/models";

  public static final String PROCESS_METADATA_FOLDER_NAME = "metadata";

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
      Map<String, String> headers = getHeaderParameters();

      DeployProcessRequest deployProcessRequest = createDeployProcessRequest();
      byte[] data = getBody(deployProcessRequest);

      ProcessDeploymentResponse response = CONNECTION_HELPER.post(environment, API_PATH, headers, data);
      logEndOfTask(response);
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }
  }

  private byte[] getBody(DeployProcessRequest deployProcessRequest) throws IOException
  {
    ObjectMapper objectMapper = new ObjectMapper();
    String deployProcessRequestAsString = objectMapper.writeValueAsString(deployProcessRequest);
    return deployProcessRequestAsString.getBytes(StandardCharsets.UTF_8);
  }

  private DeployProcessRequest createDeployProcessRequest()
  {

    Map<String, ProcessMetadata> metadata = new HashMap<>();

    List<Path> metadataFiles = readMetadataFiles();

    metadataFiles.forEach(file -> {
      String fileName = FilenameUtils.removeExtension(file.getFileName().toString());
      ProcessMetadata processMetadata = readProcessMetadata(file);
      metadata.put(fileName, processMetadata);
    });

    DeployProcessRequest deployProcessRequest = new DeployProcessRequest();
    deployProcessRequest.setMetadata(metadata);

    byte[] data = createDeploymentArchive();
    deployProcessRequest.setBarArchiveBase64(Base64.getEncoder().encodeToString(data));

    deployProcessRequest.setDeploymentName(deploymentName);

    return deployProcessRequest;
  }

  private List<Path> readMetadataFiles()
  {
    File metadataFolder = determineMetadataFolder();

    if (metadataFolder.exists())
    {
      return FileHelper.readFilesInFolder(metadataFolder.toPath());
    }
    return Collections.emptyList();
  }

  private File determineMetadataFolder()
  {
    Path processModelPath = FileHelper.getCustomFolderOrDefault(projectDir, filePath, MODEL_DIR);

    File file = processModelPath.toFile();

    if (file.isFile())
    {
      String fullPath = FilenameUtils.getFullPath(processModelPath.toString());

      return new File(fullPath, PROCESS_METADATA_FOLDER_NAME);
    }
    else
    {
      return new File(processModelPath.toFile(), PROCESS_METADATA_FOLDER_NAME);
    }
  }

  private ProcessMetadata readProcessMetadata(Path file)
  {
    try
    {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(file.toFile(), ProcessMetadata.class);
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fehler beim Lesen der Metadata-Datei " + file, e);
    }
  }

  private byte[] createDeploymentArchive()
  {
    Path folder = FileHelper.getCustomFolderOrDefault(projectDir, filePath, MODEL_DIR);

    return FileHelper.createArchiveForFilesInFolder(folder, PROCESS_METADATA_FOLDER_NAME);
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.PROCESS_DUPLICATION, duplicateProcesskeyAction.toString());
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

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
