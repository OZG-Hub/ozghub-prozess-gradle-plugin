package de.seitenbau.ozghub.prozesspipeline.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

  public static byte[] createArchiveForFilesInFolder(Path folder)
  {
    log.info("Dateien im Pfad {} lesen", folder);

    List<Path> files = new ArrayList<>();
    try
    {
      Files.walk(folder)
          .filter(Files::isRegularFile)
          .forEach(f -> files.add(f.toAbsolutePath()));
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fehler beim Lesen der Dateien in Ordner " + folder, e);
    }

    log.info("{} Datei(en) eingelesen", files.size());
    return createArchive(files);
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
