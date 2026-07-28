package de.seitenbau.ozghub.prozessdeployment.helper;

public record PlatformVersion(int major, int minor)
{
  public PlatformVersion(int major)
  {
    this(major, 1);
  }

  public boolean isNewerThan(PlatformVersion version)
  {
    return major > version.major() || major == version.major() && minor > version.minor();
  }

  @Override
  public String toString()
  {
    return "1." + major + "." + minor;
  }
}
