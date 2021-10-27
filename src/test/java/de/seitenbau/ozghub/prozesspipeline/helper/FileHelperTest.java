package de.seitenbau.ozghub.prozesspipeline.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class FileHelperTest
{
  private static final String RESOURCES_PATH = "helper/FileHelper";

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
      zis.getNextEntry();
      byte[] contentBytes = IOUtils.toByteArray(zis);
      String content = new String(contentBytes);
      assertThat(content).isEqualTo("This is the test-file.");

      zis.getNextEntry();
      contentBytes = IOUtils.toByteArray(zis);
      content = new String(contentBytes);
      assertThat(content).isEqualTo("This is the test-sub-file.");
      assertThat(zis.getNextEntry()).isNull();
    }
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
