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
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.FileHelper;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.request.DuplicateProcessKeyAction;
import de.seitenbau.ozghub.prozessdeployment.model.request.ProcessDeploymentRequest;
import de.seitenbau.ozghub.prozessdeployment.model.request.ProcessMetadata;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessDeploymentResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeployProcessHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/ozghub/deployWithMetadata";

  private static final String MODEL_DIR = "/build/models";

  public static final String PROCESS_METADATA_FOLDER_NAME = "/metadata";

  private static final ServerConnectionHelper<ProcessDeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(ProcessDeploymentResponse.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final File projectDir;

  private final String filePath;

  private final String deploymentName;

  private final String versionName;

  private final DuplicateProcessKeyAction duplicateProcesskeyAction;

  private final String engineId;

  private final String metadataFolder;

  // CHECKSTYLE:OFF ParameterNumberCheck
  public DeployProcessHandler(Environment environment,
      File projectDir,
      String filePath,
      String deploymentName,
      String versionName,
      DuplicateProcessKeyAction duplicateKeyAction,
      String engineId,
      String metadataFolder)
  {
    super(environment);
    this.projectDir = projectDir;
    this.filePath = filePath;
    this.deploymentName = deploymentName;
    this.versionName = versionName;
    this.duplicateProcesskeyAction =
        Objects.requireNonNullElse(duplicateKeyAction, DuplicateProcessKeyAction.ERROR);
    this.engineId = engineId;
    this.metadataFolder = metadataFolder;
  }
  // CHECKSTYLE:ON ParameterNumberCheck

  public void deploy()
  {
    log.info("Start des Tasks: Deployment eines Prozessmodells");

    try
    {
      Map<String, String> headers = getHeaderParameters();

      ProcessDeploymentRequest deployProcessRequest = createProcessDeploymentRequest();
      byte[] data = getRequestBody(deployProcessRequest);

      ProcessDeploymentResponse response = CONNECTION_HELPER.post(environment, API_PATH, headers, data);
      logEndOfTask(response);
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }
  }

  private byte[] getRequestBody(ProcessDeploymentRequest deployProcessRequest) throws IOException
  {
    String deployProcessRequestAsString = OBJECT_MAPPER.writeValueAsString(deployProcessRequest);
    return deployProcessRequestAsString.getBytes(StandardCharsets.UTF_8);
  }

  private ProcessDeploymentRequest createProcessDeploymentRequest()
  {
    byte[] data = createDeploymentArchive();
    Map<String, ProcessMetadata> metadata = createMetadataMap();

    return
        new ProcessDeploymentRequest(Base64.getEncoder().encodeToString(data), deploymentName, versionName,
            metadata);
  }

  private Map<String, ProcessMetadata> createMetadataMap()
  {
    Map<String, ProcessMetadata> metadata = new HashMap<>();

    List<Path> metadataFiles = getMetadataFilePathList();

    metadataFiles.forEach(file -> {
      String fileName = FilenameUtils.removeExtension(file.getFileName().toString());
      ProcessMetadata processMetadata = readProcessMetadataFromFile(file);
      metadata.put(fileName, processMetadata);
    });
    return metadata;
  }

  private List<Path> getMetadataFilePathList()
  {
    Path metadataFolder =
        FileHelper.getCustomFolderOrDefault(projectDir, this.metadataFolder, PROCESS_METADATA_FOLDER_NAME);

    if (metadataFolder.toFile().exists())
    {
      return FileHelper.readFilesInFolder(metadataFolder);
    }
    if (this.metadataFolder != null)
    {
      throw new RuntimeException("Die angegebene Quelle für Metadaten (" + metadataFolder
          + ") konnte nicht gefunden werden");
    }
    return Collections.emptyList();
  }

  private ProcessMetadata readProcessMetadataFromFile(Path file)
  {
    try
    {
      return OBJECT_MAPPER.readValue(file.toFile(), ProcessMetadata.class);
    }
    catch (InvalidFormatException e)
    {
      throw new RuntimeException(
          "Fehler beim Einlesen der Metadata-Datei " + file + " (" + e.getMessage() + ")", e);
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fehler beim Einlesen der Metadata-Datei " + file, e);
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
