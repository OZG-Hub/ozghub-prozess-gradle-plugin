package de.seitenbau.ozghub.prozessdeployment.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
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
      assertThat(content).isEqualTo("This is the test-file.");
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

      List<String> expectedContents = List.of("This is the test-file.", "This is the test-sub-file.");
      assertThat(actualContents).containsExactlyInAnyOrderElementsOf(expectedContents);
      assertThat(zis.getNextEntry()).isNull();
    }
  }

  @Test
  public void getCustomFolderOrDefault_null(@TempDir File directory)
  {
    // arrange
    File defaultDir = new File(directory, "default");
    File buildDir = new File(directory, "build");
    File buildDefaultDir = new File(buildDir, "default");
    assertThat(buildDefaultDir.mkdirs()).isTrue();

    // act
    Path actual1 = FileHelper.getCustomFolderOrDefault(directory, null, "default");
    Path actual2 = FileHelper.getCustomFolderOrDefault(directory, null, "build", "default");
    Path actual3 = FileHelper.getCustomFolderOrDefault(directory, null, "other", "default");

    // assert
    assertThat(actual1.toFile()).isEqualTo(defaultDir);
    assertThat(actual2.toFile()).isEqualTo(buildDefaultDir);
    assertThat(actual3.toFile()).isEqualTo(defaultDir);
  }

  @Test
  public void getCustomFolderOrDefault_relative(@TempDir File directory)
  {
    // arrange
    File expected = new File(directory, "custom");

    // act
    Path actual1 = FileHelper.getCustomFolderOrDefault(directory, "custom", null);
    Path actual2 = FileHelper.getCustomFolderOrDefault(directory, "custom", null, null);

    // assert
    assertThat(actual1.toFile()).isEqualTo(expected);
    assertThat(actual2.toFile()).isEqualTo(expected);
  }

  @Test
  public void getCustomFolderOrDefault_absolute_userHome(@TempDir File directory)
  {
    // arrange
    String userHome = System.getProperty("user.home");
    File expected = new File(userHome, "custom");

    // act
    Path actual1 = FileHelper.getCustomFolderOrDefault(directory, "~/custom", null);
    Path actual2 = FileHelper.getCustomFolderOrDefault(directory, "~/custom", null, null);

    // assert
    assertThat(actual1.toFile()).isEqualTo(expected);
    assertThat(actual2.toFile()).isEqualTo(expected);
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  public void getCustomFolderOrDefault_absolute_windows(@TempDir File directory)
  {
    // arrange
    File expected = new File("C:\\custom");

    // act
    Path actual1 = FileHelper.getCustomFolderOrDefault(directory, "C:\\custom", null);
    Path actual2 = FileHelper.getCustomFolderOrDefault(directory, "C:\\custom", null, null);

    // assert
    assertThat(actual1.toFile()).isEqualTo(expected);
    assertThat(actual2.toFile()).isEqualTo(expected);
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  public void getCustomFolderOrDefault_absolute_linux(@TempDir File directory)
  {
    // arrange
    File expected = new File("/custom");

    // act
    Path actual1 = FileHelper.getCustomFolderOrDefault(directory, "/custom", null);
    Path actual2 = FileHelper.getCustomFolderOrDefault(directory, "/custom", null, null);

    // assert
    assertThat(actual1.toFile()).isEqualTo(expected);
    assertThat(actual2.toFile()).isEqualTo(expected);
  }

  @Test
  @EnabledOnOs(OS.MAC)
  public void getCustomFolderOrDefault_absolute_macOS(@TempDir File directory)
  {
    // arrange
    File expected = new File("/custom");

    // act
    Path actual1 = FileHelper.getCustomFolderOrDefault(directory, "/custom", null);
    Path actual2 = FileHelper.getCustomFolderOrDefault(directory, "/custom", null, null);

    // assert
    assertThat(actual1.toFile()).isEqualTo(expected);
    assertThat(actual2.toFile()).isEqualTo(expected);
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
