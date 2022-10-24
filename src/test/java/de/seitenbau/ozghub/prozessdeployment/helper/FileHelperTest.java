package de.seitenbau.ozghub.prozessdeployment.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

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
    Path expected = Path.of(projectDir.getPath(), "custom");

    // act
    Path actual = FileHelper.getCustomFolderOrDefault(projectDir, "custom", null);

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
