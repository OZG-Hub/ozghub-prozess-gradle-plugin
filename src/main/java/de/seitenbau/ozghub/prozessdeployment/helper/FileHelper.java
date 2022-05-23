package de.seitenbau.ozghub.prozessdeployment.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class FileHelper
{
  private FileHelper()
  {
  }

  public static byte[] createArchiveForFilesInFolder(Path folder, String... subfoldersToExclude)
  {
    List<Path> files = readFilesInFolder(folder, subfoldersToExclude);

    return createArchive(files);
  }

  public static List<Path> readFilesInFolder(Path folder, String... subfoldersToExclude)
  {
    List<Path> files = new ArrayList<>();

    try
    {
      Files.walk(folder)
          .filter(Files::isRegularFile)
          .filter(path -> includeFile(folder, path, subfoldersToExclude))
          .forEach(f -> files.add(f.toAbsolutePath()));
      log.info("Im Ordner {} wurden {} Dateien gefunden.", folder, files.size());
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fehler beim Lesen der Dateien in Ordner " + folder, e);
    }

    return files;
  }

  private static boolean includeFile(Path folder, Path filePath, String... subfoldersToExclude)
  {
    for (String folderName : subfoldersToExclude)
    {
      if (folder.relativize(filePath.getParent()).toString().contains(folderName))
      {
        return false;
      }
    }
    return true;
  }

  public static Path getCustomFolderOrDefault(File projectDir, String customFolder, String defaultFolder)
  {
    if (customFolder != null)
    {
      return new File(customFolder).toPath();
    }
    return Paths.get(projectDir.getPath(), defaultFolder);
  }

  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  private static byte[] createArchive(List<Path> files)
  {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
    {
      try (ZipOutputStream zout = new ZipOutputStream(baos))
      {
        for (Path file : files)
        {
          ZipEntry entry = new ZipEntry(file.getFileName().toString());
          byte[] fileContent = readAllBytes(file);

          try (InputStream fin = new ByteArrayInputStream(fileContent))
          {
            zout.putNextEntry(entry);
            IOUtils.copy(fin, zout);
            zout.closeEntry();
          }
        }
      }

      return baos.toByteArray();
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fehler beim Erzeugen des ZIP-Archivs", e);
    }
  }

  private static byte[] readAllBytes(Path path)
  {
    try
    {
      return Files.readAllBytes(path);
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fehler beim Lesen der Datei " + path, e);
    }
  }
}
