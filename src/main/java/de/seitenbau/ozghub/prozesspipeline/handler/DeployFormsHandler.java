package de.seitenbau.ozghub.prozesspipeline.handler;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  private static final String DEFAULT_FORMS_DIR_NAME = "forms";

  private static final ServerConnectionHelper<FormDeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(FormDeploymentResponse.class);

  private final File projectDir;

  private final String formFilesDir;

  public DeployFormsHandler(Environment env,
      File projectDir, String formFilesDir)
  {
    super(env);
    this.projectDir = projectDir;
    this.formFilesDir = formFilesDir;
  }

  public void deploy()
  {
    log.info("Start des Tasks: Deployment von Formularen");

    try
    {
      List<Path> files = Files.walk(getCustomFormsDirOrDefault(formFilesDir)).collect(Collectors.toList());

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
        byte[] formJson = Files.readAllBytes(path);
        String formName = readFormId(formJson);

        String apiPath = API_PATH + "/" + URLEncoder.encode(formName, StandardCharsets.UTF_8);

        FormDeploymentResponse response =
            CONNECTION_HELPER.post(environment, apiPath, headers, formJson);

        log.info("Das Deployment von Formular {} wurde erfolgreich abgeschlossen. " +
                "ID des Deployments: {}",
            formName, response.getDeploymentId());
      }
    }
  }

  private String readFormId(byte[] formJson) throws java.io.IOException
  {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode formAndMapping = objectMapper.readValue(formJson, ObjectNode.class);
    return formAndMapping.get("id").toString();
  }

  private Path getCustomFormsDirOrDefault(String formsDir)
  {
    if (formsDir != null)
    {
      return Paths.get(formsDir);
    }
    return Paths.get(projectDir.getPath(), DEFAULT_FORMS_DIR_NAME);
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

    return headers;
  }
}
