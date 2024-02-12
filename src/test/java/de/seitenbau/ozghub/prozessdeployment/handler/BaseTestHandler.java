package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public abstract class BaseTestHandler
{
  protected static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

  private static ObjectMapper createObjectMapper()
  {
    ObjectMapper objectMapper = new ObjectMapper();
    // Jackson hat aktuell (19.01.2024) keinen nativen Support für LocalDate, erweitere jackson
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  protected abstract File getTestFolder();

  protected File getProjectDir()
  {
    return getTestFolder();
  }

  protected File getFileInProjectDir(String path)
  {
    return new File(getTestFolder() + path);
  }
}
