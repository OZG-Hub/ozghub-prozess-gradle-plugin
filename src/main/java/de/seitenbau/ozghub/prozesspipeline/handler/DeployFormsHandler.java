package de.seitenbau.ozghub.prozesspipeline.handler;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.GradleException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.seitenbau.ozghub.prozesspipeline.common.Environment;
import de.seitenbau.ozghub.prozesspipeline.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozesspipeline.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozesspipeline.model.response.FormDeploymentResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeployFormsHandler extends DefaultHandler
{
  public static final String API_PATH = "/formulare/ozghub";

  private static final String DEFAULT_FORMS_DIR = "/forms";

  private static final ServerConnectionHelper<FormDeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(FormDeploymentResponse.class);

  public static final String FILE_EXTENSION_JSON = "json";

  private final File projectDir;

  private final String formFiles;

  public DeployFormsHandler(Environment env,
      File projectDir, String formFiles)
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
      List<Path> files = new ArrayList<>();
      Path folder = getCustomFormsDirOrDefault(formFiles);

      Files.walk(folder).filter(Files::isRegularFile)
          .forEach(f -> files.add(f.toAbsolutePath()));

      Map<String, String> headers = getHeaderParameters();
      deployFiles(headers, files);
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }
  }

  private void deployFiles(Map<String, String> headers, List<Path> filePaths) throws java.io.IOException
  {
    for (Path path : filePaths)
    {
      File file = path.toFile();

      if (file.isDirectory())
      {
        deployFiles(headers, Files.walk(path).collect(Collectors.toList()));
      }
      else
      {
        if (!FILE_EXTENSION_JSON.equals(FilenameUtils.getExtension(file.getName())))
        {
          log.info("Datei {} scheint keine json-Datei zu sein und wird übersprungen.",
              file.getName());
          return;
        }

        byte[] formJson = Files.readAllBytes(path);
        String formId = readFormId(formJson);

        String apiPath = API_PATH + "/" + URLEncoder.encode(formId, StandardCharsets.UTF_8);

        FormDeploymentResponse response =
            CONNECTION_HELPER.post(environment, apiPath, headers, formJson);

        log.info("Das Deployment von Formular {} wurde erfolgreich abgeschlossen. " +
                "ID des Deployments: {}",
            formId, response.getDeploymentId());
      }
    }
  }

  private String readFormId(byte[] formJson) throws java.io.IOException
  {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode formAndMapping = objectMapper.readValue(formJson, ObjectNode.class);
    return formAndMapping.get("id").textValue();
  }

  private Path getCustomFormsDirOrDefault(String formsDir)
  {
    if (formsDir != null)
    {
      return new File(formsDir).toPath();
    }
    return Paths.get(projectDir.getPath(), DEFAULT_FORMS_DIR);
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

    return headers;
  }
}
