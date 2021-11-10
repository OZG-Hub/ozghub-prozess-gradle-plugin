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
import de.seitenbau.ozghub.prozesspipeline.model.response.ProcessDeploymentResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeployFormsHandler extends DefaultHandler
{
  public static final String API_PATH = "/formulare/ozghub/deploy/";

  private static final String FORMS_DIR = "/forms";

  private static final ServerConnectionHelper<FormDeploymentResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(FormDeploymentResponse.class);

  private final File projectDir;

  //TODO beliebigen Ordner ermöglichen

  public DeployFormsHandler(Environment env,
      File projectDir)
  {
    super(env);
    this.projectDir = projectDir;
  }

  public void deploy()
  {
    log.info("Start des Tasks: Deployment von Formularen");

    Map<String, String> headers = getHeaderParameters();

    Path formsDir = Paths.get(projectDir.getPath(), FORMS_DIR);

    try
    {
      List<Path> files = Files.walk(formsDir).collect(Collectors.toList());

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
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode formAndMapping = objectMapper.readValue(file, ObjectNode.class);


        String formName = formAndMapping.get("id").toString();
        //TODO beliebige Version ermöglichen?
        String apiPath = API_PATH + URLEncoder.encode(formName, StandardCharsets.UTF_8) + "/v1.0";
        FormDeploymentResponse response =
            CONNECTION_HELPER.post(environment, apiPath, headers, Files.readAllBytes(path));
        log.info("Das Deployment von Formular {} wurde erfolgreich abgeschlossen. " +
                "ID des Deployments: {}",
            formName, response.getDeploymentId());
      }
    }
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

    return headers;
  }
}
