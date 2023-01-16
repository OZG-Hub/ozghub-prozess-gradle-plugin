package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.FileHelper;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.request.EncryptParameterValueRequest;
import de.seitenbau.ozghub.prozessdeployment.model.response.EncryptParameterValueResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EncryptParameterValueHandler extends DefaultHandler
{
  /**
   * The URL to the endpoint in the servicegateway-ozg
   */
  public static final String API_PATH = "/prozessparameter/parameter/encryptParameterValue";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final ServerConnectionHelper<EncryptParameterValueResponse> CONNECTION_HELPER =
      new ServerConnectionHelper<>(EncryptParameterValueResponse.class);

  private final File projectDir;

  private final String processKey;

  private final String parameterValue;

  private final String inputFilePath;

  private final String charsetName;

  private final boolean base64;

  private final String outputFilePath;

  // CHECKSTYLE:OFFF ParameterNumber
  public EncryptParameterValueHandler(
      Environment environment,
      File projectDir,
      String processKey,
      String parameterValue,
      String inputFilePath,
      String charsetName,
      boolean base64,
      String outputFilePath)
  {
    super(environment);
    this.projectDir = projectDir;
    this.processKey = processKey;
    this.parameterValue = parameterValue;
    this.inputFilePath = inputFilePath;
    this.charsetName = charsetName;
    this.base64 = base64;
    this.outputFilePath = outputFilePath;
  }
  // CHECKSTYLE:ON ParameterNumber

  public void encryptParameterValue()
  {
    log.info("Start des Tasks: encryptParameterValue");

    try
    {
      String value = getParameterValue();
      EncryptParameterValueResponse response = encrypt(value);
      logOrStoreResult(response);
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }

    log.info("Ende des Tasks: encryptParameterValue");
  }

  private String getParameterValue() throws IOException
  {
    // Als String
    if (parameterValue != null)
    {
      if (inputFilePath != null)
      {
        throw new IllegalArgumentException("Der zu verschluesselnde Parameter muss entweder als Text oder als"
            + " Datei angegeben werden. Beides ist nicht erlaubt.");
      }

      return encodeBase64(parameterValue);
    }

    // Als Datei
    if (inputFilePath != null)
    {
      Path path = FileHelper.getCustomFolderOrDefault(projectDir, inputFilePath, null);

      if (base64)
      {
        byte[] content = FileUtils.readFileToByteArray(path.toFile());
        return Base64.getEncoder().encodeToString(content);
      }

      Charset charset = FileHelper.getCharset(charsetName, StandardCharsets.UTF_8);
      return FileUtils.readFileToString(path.toFile(), charset);
    }

    throw new IllegalArgumentException("Der zu verschluesselnde Parameter muss entweder als Text oder als"
        + " Datei angegeben werden.");
  }

  private String encodeBase64(String str)
  {
    return base64 ? Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8)) : str;
  }

  private EncryptParameterValueResponse encrypt(String plaintext) throws IOException
  {
    Map<String, String> headers = getHeaderParameters();
    EncryptParameterValueRequest request = EncryptParameterValueRequest.builder()
        .processKey(processKey)
        .parameterValue(plaintext)
        .build();
    byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(request);

    return CONNECTION_HELPER.post(environment, API_PATH, headers, bytes);
  }

  private void logOrStoreResult(EncryptParameterValueResponse response) throws IOException
  {
    log.info("Die Verschlüsselung des Parameterwertes wurde erfolgreich abgeschlossen.");

    // Loggen
    if (outputFilePath == null)
    {
      log.info("Der verschlüsselte Parameterwert ist: {}", response.getEncryptedParameterValue());
      return;
    }

    // In Datei schreiben
    Path path = FileHelper.getCustomFolderOrDefault(projectDir, outputFilePath, null).toAbsolutePath();
    File file = path.toFile();

    if (file.exists())
    {
      throw new RuntimeException("Die Ausgabe-Datei (" + path + ") existiert bereits. Der verschlüsselte"
          + " Parameterwert kann nur in eine neue, noch nicht existierende Datei geschrieben werden.");
    }

    FileUtils.writeStringToFile(file, response.getEncryptedParameterValue(), StandardCharsets.UTF_8);
    log.info("Der verschlüsselte Parameterwert wurde in die folgende Datei geschrieben: {}", path);
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

    return headers;
  }
}
