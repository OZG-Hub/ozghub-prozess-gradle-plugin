package de.seitenbau.ozghub.prozessdeployment.handler;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
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

  private final String processKey;

  private final String parameterValue;

  public EncryptParameterValueHandler(Environment env, String processKey, String parameterValue)
  {
    super(env);
    this.processKey = processKey;
    this.parameterValue = parameterValue;
  }

  public void encryptParameterValue()
  {
    log.info("Start des Tasks: encryptParameterValue");

    try
    {
      Map<String, String> headers = getHeaderParameters();
      EncryptParameterValueRequest encryptParameterValueRequest = EncryptParameterValueRequest.builder()
          .processKey(processKey)
          .parameterValue(parameterValue)
          .build();

      byte[] encryptedParameterValueRequestBytes =
          OBJECT_MAPPER.writeValueAsBytes(encryptParameterValueRequest);

      EncryptParameterValueResponse encryptParameterValueResponse =
          CONNECTION_HELPER.post(environment, API_PATH, headers, encryptedParameterValueRequestBytes);

      log.info("Die Verschluesselung des Parameterwertes '{}' wurde erfolgreich abgeschlossen.",
          parameterValue);
      log.info("Der verschluesselte Parameterwert ist: {}",
          encryptParameterValueResponse.getEncryptedParameterValue());
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }

    log.info("Ende des Tasks: encryptParameterValue");
  }

  private Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

    return headers;
  }
}
