package de.seitenbau.ozghub.prozessdeployment.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import de.seitenbau.ozghub.prozessdeployment.model.ScheduledUndeployment;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CreateScheduledUndeploymentOzgHandler extends DefaultHandler
{
  public static final String API_PATH = "/prozess/scheduled/undeployment";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ServerConnectionHelper<Void> serverConnectionHelper = new ServerConnectionHelper<>(null);

  public CreateScheduledUndeploymentOzgHandler(Environment environment)
  {
    super(environment);
    // Jackson hat aktuell (19.01.2024) keinen nativen Support für LocalDate, erweitere jackson
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  public void createScheduledUndeploymentOzg(ScheduledUndeployment scheduledUndeployment)
  {
    log.info("Start des Tasks: createScheduledUndeploymentOzg");
    try
    {
      String payloadString = OBJECT_MAPPER.writeValueAsString(scheduledUndeployment);
      byte[] payload = payloadString.getBytes(StandardCharsets.UTF_8);

      serverConnectionHelper.post(
          environment,
          API_PATH,
          getHeaders(),
          payload);

      log.info("Das zeitgesteuerte Undeployment wurde erfolgreich erstellt");
      log.info("Ende des Tasks: createScheduledUndeploymentOzg");
    }
    catch (Exception e)
    {
      throw new RuntimeException(
          "Fehler beim Erstellen eines zeitgesteuerten Undeployment eines Online-Dienstes: " + e.getMessage(),
          e);
    }
  }

  private Map<String, String> getHeaders()
  {
    return Map.of(HTTPHeaderKeys.CONTENT_TYPE, "application/json");
  }
}
