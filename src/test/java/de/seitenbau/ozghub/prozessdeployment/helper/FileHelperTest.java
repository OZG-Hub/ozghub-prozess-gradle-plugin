package de.seitenbau.ozghub.prozessdeployment.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import lombok.SneakyThrows;

public class FileHelperTest
{
  private static final String RESOURCES_PATH = "helper/fileHelper";

  @TempDir
  private File directory;

  @Test
  @SneakyThrows
  public void createArchiveForFilesInFolder_file()
  {
    // arrange
    Path provided = getPathToFile();

    // act
    byte[] actual = FileHelper.createArchiveForFilesInFolder(provided);

    // assert
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(actual)))
    {
      zis.getNextEntry();
      byte[] contentBytes = IOUtils.toByteArray(zis);
      String content = new String(contentBytes);
      assertThat(content).isEqualTo("This is the test-file. äöüß");
      assertThat(zis.getNextEntry()).isNull();
    }
  }

  @Test
  @SneakyThrows
  public void createArchiveForFilesInFolder_folder()
  {
    // arrange
    Path provided = getPathToFolder();

    // act
    byte[] actual = FileHelper.createArchiveForFilesInFolder(provided);

    // assert
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(actual)))
    {
      List<String> actualContents = new ArrayList<>();

      zis.getNextEntry();
      byte[] contentBytes = IOUtils.toByteArray(zis);
      actualContents.add(new String(contentBytes));

      zis.getNextEntry();
      contentBytes = IOUtils.toByteArray(zis);
      actualContents.add(new String(contentBytes));

      List<String> expectedContents = List.of("This is the test-file. äöüß", "This is the test-sub-file.");
      assertThat(actualContents).containsExactlyInAnyOrderElementsOf(expectedContents);
      assertThat(zis.getNextEntry()).isNull();
    }
  }

  @Test
  public void getCustomFolderOrDefault_null()
  {
    // arrange
    File projectDir = getPathToFolder().toFile();
    Path expected = Path.of(projectDir.getPath(), "default");

    // act
    Path actual = FileHelper.getCustomFolderOrDefault(projectDir, null, "default");

    // assert
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getCustomFolderOrDefault_relative()
  {
    // arrange
    File projectDir = getPathToFolder().toFile();
    Path expected = Path.of(projectDir.getPath(), "custom");

    // act
    Path actual = FileHelper.getCustomFolderOrDefault(projectDir, "custom", "default");

    // assert
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  public void getCustomFolderOrDefault_absolute_windows()
  {
    // arrange
    File projectDir = getPathToFolder().toFile();
    Path expected = Path.of("C:\\custom");

    // act
    Path actual = FileHelper.getCustomFolderOrDefault(projectDir, "C:\\custom", null);

    // assert
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  public void getCustomFolderOrDefault_absolute_linux()
  {
    // arrange
    File projectDir = getPathToFolder().toFile();
    Path expected = Path.of("/custom");

    // act
    Path actual = FileHelper.getCustomFolderOrDefault(projectDir, "/custom", null);

    // assert
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getCustomFolderOrDefault_tilde()
  {
    // arrange
    File projectDir = getPathToFolder().toFile();
    String userHome = System.getProperty("user.home");
    Path expected = Path.of(userHome, "custom");

    // act
    Path actual = FileHelper.getCustomFolderOrDefault(projectDir, "~/custom", null);

    // assert
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getCharset()
  {
    // act
    Charset actual1 = FileHelper.getCharset("ASCII", null);
    Charset actual2 = FileHelper.getCharset("UTF-8", null);
    Charset actual3 = FileHelper.getCharset("utf-8", null);
    Charset actual4 = FileHelper.getCharset("ISO-8859-1", null);
    Charset actual5 = FileHelper.getCharset(null, StandardCharsets.UTF_8);

    // assert
    assertThat(actual1).isEqualTo(StandardCharsets.US_ASCII);
    assertThat(actual2).isEqualTo(StandardCharsets.UTF_8);
    assertThat(actual3).isEqualTo(StandardCharsets.UTF_8);
    assertThat(actual4).isEqualTo(StandardCharsets.ISO_8859_1);
    assertThat(actual5).isEqualTo(StandardCharsets.UTF_8);
  }

  @Test
  public void getCharset_notSupported()
  {
    // act
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> FileHelper.getCharset("invalid", null))
        .withMessage("Das Charset invalid wird nicht unterstützt.")
        .withRootCauseExactlyInstanceOf(UnsupportedCharsetException.class);
  }

  @Test
  public void readFile() throws IOException
  {
    // arrange
    Path provided = getPathToFile();

    // act
    String actual = FileHelper.readFile(provided, StandardCharsets.UTF_8);

    // assert
    assertThat(actual).isEqualTo("This is the test-file. äöüß");
  }

  @Test
  public void readFile_exception_notARegularFile()
  {
    // arrange
    File folder = new File(directory, "test");
    folder.mkdir();
    Path provided = folder.toPath();

    // act
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> FileHelper.readFile(provided, StandardCharsets.US_ASCII))
        .withMessage("Die Datei (" + provided.toAbsolutePath() + ") konnte nicht gelesen werden,"
            + " da es keine normale Datei ist.");
  }

  @Test
  public void readFile_exception_fileNotFound()
  {
    // arrange
    Path provided = Path.of("does", "not", "exist");

    // act
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> FileHelper.readFile(provided, StandardCharsets.US_ASCII))
        .withMessage("Die Datei (" + provided.toAbsolutePath() + ") konnte nicht gefunden werden.");
  }

  @Test
  public void readFile_exception_invalidCharset()
  {
    // arrange
    Path provided = getPathToFile();

    // act
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> FileHelper.readFile(provided, StandardCharsets.US_ASCII))
        .withMessage("Die Datei (" + provided.toAbsolutePath()
            + ") konnte nicht gelesen werden. Die Datei kann nicht US-ASCII kodiert gelesen werden.")
        .withRootCauseExactlyInstanceOf(MalformedInputException.class);
  }

  @Test
  public void writeFile() throws IOException
  {
    // arrange
    File file = new File(directory, "test.txt");
    Path provided = file.toPath();

    // act
    FileHelper.writeFile(provided, "abc 123 äöüß");
    FileHelper.writeFile(provided, "\nABC 456 ÄÖÜẞ");

    // assert
    String actual = Files.readString(provided, StandardCharsets.UTF_8);
    assertThat(actual).isEqualTo("abc 123 äöüß\nABC 456 ÄÖÜẞ");
  }

  @Test
  public void writeFile_exception_notARegularFile()
  {
    // arrange
    File folder = new File(directory, "test");
    folder.mkdir();
    Path provided = folder.toPath();

    // act & assert
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> FileHelper.writeFile(provided, ""))
        .withMessage("In die Datei (" + provided.toAbsolutePath() + ") konnte nicht geschrieben werden,"
            + " da es keine normale Datei ist.");
  }

  private Path getPathToFolder()
  {
    return getPathFromResources(RESOURCES_PATH);
  }

  private Path getPathToFile()
  {
    return getPathFromResources(RESOURCES_PATH + "/TestFile.txt");
  }

  @SneakyThrows
  private Path getPathFromResources(String path)
  {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Objects.requireNonNull(url);

    return Path.of(url.toURI());
  }
}
