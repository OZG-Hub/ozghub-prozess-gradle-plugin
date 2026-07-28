package de.seitenbau.ozghub.prozessdeployment.helper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;

public class PlatformVersionChecker
{
  private static final Pattern VERSION_PATTERN = Pattern.compile("^1\\.(\\d+)\\.(\\d+)$");

  private static final String API_PATH = "/version";

  private static final ServerConnectionHelper<String> SERVER_CONNECTION_HELPER =
      new ServerConnectionHelper<>(new TypeReference<>()
      {
      });

  private PlatformVersionChecker()
  {
  }

  public static void check(Environment env, PlatformVersion requiredMinVersion)
  {
    PlatformVersion actualVersion = getServiceportalVersion(env);
    if (actualVersion == null)
    {
      return;
    }

    if (requiredMinVersion.isNewerThan(actualVersion))
    {
      throw new RuntimeException("Der Plugin-Task ist nicht mit der Version der angesprochenen Umgebung"
          + " kompatibel. Der Task benötigt mind. Version " + requiredMinVersion + ". Es ist aber Version "
          + actualVersion);
    }
  }

  private static PlatformVersion getServiceportalVersion(Environment env)
  {
    try
    {
      String version = SERVER_CONNECTION_HELPER.get(env, API_PATH, getHeaders());
      return parseVersion(version);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Fehler bei Prüfung der Version der angesprochenen Umgebung: "
          + e.getMessage(), e);
    }
  }

  private static Map<String, String> getHeaders()
  {
    return Map.of(HTTPHeaderKeys.ACCEPT, "text/plain");
  }

  private static PlatformVersion parseVersion(String str)
  {
    Matcher matcher = VERSION_PATTERN.matcher(str);
    if (!matcher.matches())
    {
      return null;
    }

    try
    {
      int major = Integer.parseInt(matcher.group(1));
      int minor = Integer.parseInt(matcher.group(2));
      return new PlatformVersion(major, minor);
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException("'" + str + "' ist keine gültige Version", e);
    }
  }
}
