package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import de.seitenbau.ozghub.prozessdeployment.helper.ServerConnectionHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractListHandler<T> extends DefaultHandler
{
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private final Class<T> responseType;

  private final String apiPath;

  public AbstractListHandler(Environment environment, Class<T> responseType, String apiPath)
  {
    super(environment);
    this.responseType = responseType;
    this.apiPath = apiPath;
  }

  protected String formatDate(Date deploymentDate)
  {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    return format.format(deploymentDate);
  }

  protected Map<String, String> getHeaderParameters()
  {
    Map<String, String> headers = new HashMap<>();
    headers.put(HTTPHeaderKeys.CONTENT_TYPE, "application/json");

    return headers;
  }

  public void list(String taskName)
  {
    log.info("Start des Tasks: " + taskName);

    try
    {
      T list = getList();
      writeLogEntries(list);
    }
    catch (Exception e)
    {
      throw new GradleException("Fehler: " + e.getMessage(), e);
    }
    log.info("Ende des Tasks: " + taskName);
  }

  protected abstract void writeLogEntries(T list) throws IOException;

  protected T getList() throws java.io.IOException
  {
    ServerConnectionHelper<T> serverConnectionHelper = new ServerConnectionHelper<>(responseType);
    return serverConnectionHelper.get(environment, apiPath, getHeaderParameters());
  }
}
