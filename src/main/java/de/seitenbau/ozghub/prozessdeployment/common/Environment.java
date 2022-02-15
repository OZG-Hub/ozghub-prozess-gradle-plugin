package de.seitenbau.ozghub.prozessdeployment.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Environment
{
  /** URL zu einer OZG-Hub-Umgebung. */
  private final String url;

  /** Benutzername zur Authentifizierung. */
  private final String user;

  /** Password zur Authentifizierung. */
  private final String password;
}
