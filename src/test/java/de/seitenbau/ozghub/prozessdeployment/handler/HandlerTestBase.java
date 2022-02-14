package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class HandlerTestBase
{
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
