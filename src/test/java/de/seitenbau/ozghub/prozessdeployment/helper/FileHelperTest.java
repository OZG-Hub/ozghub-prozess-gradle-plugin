package de.seitenbau.ozghub.prozessdeployment.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class FileHelperTest
{
  private static final String RESOURCES_PATH = "helper/fileHelper";

  @Test
  @SneakyThrows
  public void createArchiveForFilesInFolder_file()
  {
    // arrange
    Path provided = getPathFromResources(Path.of(RESOURCES_PATH, "createArchive", "TestFile.txt"));

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
    Path provided = getPathFromResources(Path.of(RESOURCES_PATH, "createArchive"));

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
  public void readFilesFromFolder()
  {
    //arranger
    Path provided = getPathFromResources(Path.of(RESOURCES_PATH, "readFiles"));

    //act
    List<Path> actualFiles = FileHelper.readFilesInFolder(provided);

    //assert
    assertThat(actualFiles).hasSize(2);
    assertThat(actualFiles).containsExactlyInAnyOrder(
        Path.of(provided.toString(), "TestFile.txt"),
        Path.of(provided.toString(), "subfolder", "TestSubFile.txt"));
  }


  @SneakyThrows
  private Path getPathFromResources(Path path)
  {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path.toString());
    Objects.requireNonNull(url);

    return Path.of(url.toURI());
  }
}
