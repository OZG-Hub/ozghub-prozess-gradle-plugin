package de.seitenbau.ozghub.prozessdeployment.task;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import de.seitenbau.ozghub.prozessdeployment.handler.EncryptParameterValueHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncryptParameterValueTask extends DefaultPluginTask
{
  /**
   * Der Prozess-Schlüssel (Process Key)
   */
  @Input
  private String processKey;

  /**
   * Der zu verschlüsselnde Parameterwert.
   */
  @Input
  @Optional
  private String parameterValue;

  /**
   * Der zu verschlüsselnde Parameterwert ist der Dateiinhalt der Datei.
   */
  @Input
  @Optional
  private String inputFile;

  /**
   * Der Character-Set in dem der Dateiinhalt gelesen werden soll. Default ist UTF-8.
   * Nur relevant, wenn der zu geschlüsselnde Wert aus einer Datei gelesen wird.
   */
  @Input
  @Optional
  private String charset;

  /**
   * {@code true}, wenn der Dateiinhalt vor dem Verschlüsseln Base64-kordiert werden soll.
   * Default ist {@code false}. Nur relevant, wenn der zu geschlüsselnde Wert aus einer Datei gelesen wird.
   */
  @Input
  @Optional
  private String base64;

  /**
   * Wenn gesetzt, wird der verschlüsselte Parameter nicht in der Console ausgegeben, sondern in die gegebene
   * Datei geschrieben.
   */
  @Input
  @Optional
  private String outputFile;

  @TaskAction
  public void run()
  {
    boolean encodeBase64 = Boolean.parseBoolean(base64);
    EncryptParameterValueHandler handler = new EncryptParameterValueHandler(getEnvironment(), getProjectDir(),
        processKey, parameterValue, inputFile, charset, encodeBase64, outputFile);

    handler.encryptParameterValue();
  }
}
