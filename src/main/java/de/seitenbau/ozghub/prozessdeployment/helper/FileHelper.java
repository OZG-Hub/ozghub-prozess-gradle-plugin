package de.seitenbau.ozghub.prozessdeployment.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class FileHelper
{
  private static final Pattern HOME_FOLDER_PATTERN = Pattern.compile("^~");

  private FileHelper()
  {
  }

  public static byte[] createArchiveForFilesInFolder(Path folder)
  {
    List<Path> files = readFilesInFolder(folder);

    return createArchive(files);
  }

  public static List<Path> readFilesInFolder(Path folder)
  {
    List<Path> files = new ArrayList<>();

    try (Stream<Path> pathStream = Files.walk(folder))
    {
      pathStream
          .filter(Files::isRegularFile)
          .forEach(f -> files.add(f.toAbsolutePath()));
      log.info("Im Ordner {} wurden {} Dateien gefunden.", folder, files.size());
    }
    catch (IOException e)
    {
      throw new RuntimeException("Fehler beim Lesen der Dateien in Ordner " + folder, e);
    }

    return files;
  }

  public static Path getCustomFolderOrDefault(File projectDir, String customFolder, String defaultFolder)
  {
    if (customFolder != null)
    {
      String userHome = System.getProperty("user.home");
      String actualFolder = HOME_FOLDER_PATTERN
          .matcher(customFolder)
          .replaceFirst(Matcher.quoteReplacement(userHome));
      Path path = Path.of(actualFolder);
      return path.isAbsolute() ? path : Paths.get(projectDir.getPath(), actualFolder);
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

  public static Charset getCharset(String charset, Charset nullDefault)
  {
    try
    {
      return charset == null ? nullDefault : Charset.forName(charset);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Das Charset " + charset + " wird nicht unterstützt.", e);
    }
  }

  public static String readFile(Path path, Charset charset)
  {
    File file = path.toFile();

    if (!file.exists())
    {
      throw new RuntimeException("Die Datei (" + path.toAbsolutePath() + ") konnte nicht gefunden werden.");
    }
    if (!file.isFile())
    {
      throw new RuntimeException("Die Datei (" + path.toAbsolutePath() + ") konnte nicht gelesen werden,"
          + " da es keine normale Datei ist.");
    }

    try
    {
      return Files.readString(path, charset);
    }
    catch (MalformedInputException e)
    {
      throw new RuntimeException("Die Datei (" + path.toAbsolutePath() + ") konnte nicht gelesen werden."
          + " Die Datei kann nicht " + charset + " kodiert gelesen werden.", e);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Die Datei (" + path.toAbsolutePath() + ") konnte nicht gelesen werden: "
          + e.getMessage(), e);
    }
  }

  public static void writeFile(Path path, String content) throws IOException
  {
    File file = path.toFile();
    file.createNewFile();

    if (!file.isFile())
    {
      throw new RuntimeException("In die Datei (" + path.toAbsolutePath() + ") konnte nicht geschrieben"
          + " werden, da es keine normale Datei ist.");
    }

    try
    {
      Files.writeString(path, content, StandardOpenOption.APPEND);
    }
    catch (Exception e)
    {
      throw new RuntimeException("In die Datei (" + path.toAbsolutePath() + ") konnte nicht geschrieben"
          + " werden: " + e.getMessage(), e);
    }
  }
}
