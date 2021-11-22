package de.seitenbau.ozghub.prozesspipeline.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.GradleException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.helper.FileHelper;
import de.seitenbau.ozghub.prozesspipeline.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozesspipeline.model.response.FormDeploymentResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeployFormsHandler extends DefaultHandler
{
  public static final String API_PATH = "/formulare/ozghub/deploy";

  private static final String DEFAULT_FORMS_DIR = "/forms";

  private static final ServerConnectionHelper<FormDeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(FormDeploymentResponse.class);

  public static final String FILE_EXTENSION_JSON = "json";

  private final File projectDir;

  private final String formFiles;

  public DeployFormsHandler(Environment env, File projectDir, String formFiles)
  {
    super(env);
    this.projectDir = projectDir;
    this.formFiles = formFiles;
  }

  public void deploy()
  {
    log.info("Start des Tasks: Deployment von Formularen");

    try
    {
      Path path = FileHelper.getCustomFolderOrDefault(projectDir, formFiles, DEFAULT_FORMS_DIR);
      List<Path> files = FileHelper.readFilesInFolder(path);

      Map<String, String> headers = getHeaderParameters();
      int numberOfDeployedFiles = deployFiles(headers, files);

      log.info("Es wurden {} Dateien erfolgreich deployed.", numberOfDeployedFiles);
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }

    log.info("Ende des Tasks: Deployment von Formularen");
  }

  private int deployFiles(Map<String, String> headers, List<Path> filePaths) throws java.io.IOException
  {
    int deployedFilesCounter = 0;
    for (Path path : filePaths)
    {
      File file = path.toFile();

      if (!FILE_EXTENSION_JSON.equals(FilenameUtils.getExtension(file.getName())))
      {
        log.info("Datei {} scheint keine json-Datei zu sein und wird übersprungen.",
            file.getName());
        continue;
      }

      log.info("Deploye Datei {}.", file.getName());
      deployFile(headers, path);
      deployedFilesCounter++;
    }
    return deployedFilesCounter;
  }

  private void deployFile(Map<String, String> headers, Path path) throws IOException
  {
    byte[] formJson = Files.readAllBytes(path);
    String formId = readFormId(formJson);

    FormDeploymentResponse response =
        CONNECTION_HELPER.post(environment, API_PATH, headers, formJson);

    log.info("Das Deployment von Formular {} wurde erfolgreich abgeschlossen. "
            + "ID des Deployments: {}",
        formId, response.getDeploymentId());
  }

  private String readFormId(byte[] formJson) throws java.io.IOException
  {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode formAndMapping = objectMapper.readValue(formJson, ObjectNode.class);
    return formAndMapping.get("id").textValue();
  }


  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

    return headers;
  }
}
