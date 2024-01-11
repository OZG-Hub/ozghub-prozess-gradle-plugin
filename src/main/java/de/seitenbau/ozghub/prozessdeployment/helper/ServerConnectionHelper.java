package de.seitenbau.ozghub.prozessdeployment.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServerConnectionHelper<T>
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final Class<T> responseType;

  public ServerConnectionHelper(Class<T> responseType)
  {
    this.responseType = responseType;
  }

  /**
   * GET-Request an eine Schnittstelle senden. Die URL der Schnittstelle setzt sich aus der URL des
   * Environments und dem Pfad der API zusammen. Es sind nur HTTP oder HTTPS-URLs möglich.
   *
   * @param env Informationen über den Server der Schnittstelle
   * @param path Pfad der API
   * @param headers HTTP-Header
   *
   * @return Rückgabewert des Servers
   * @throws IOException Wenn beim Aufrufen der Schnittstelle oder beim Bearbeiten der Anfrage beim Server ein
   * Fehler aufgetreten ist
   */
  public T get(Environment env, String path, Map<String, String> headers) throws IOException
  {
    try (InputStream stream = getInternal(env, path, headers))
    {
      return MAPPER.readValue(stream, responseType);
    }
  }

  /**
   * POST-Request an eine Schnittstelle senden. Die URL der Schnittstelle setzt sich aus der URL des
   * Environments und dem Pfad der API zusammen. Es sind nur HTTP oder HTTPS-URLs möglich.
   *
   * @param env Informationen über den Server der Schnittstelle
   * @param path Pfad der API
   * @param headers HTTP-Header
   * @param data Daten im Body des Request
   *
   * @return Rückgabewert des Servers
   * @throws IOException Wenn beim Aufrufen der Schnittstelle oder beim Bearbeiten der Anfrage beim Server ein
   * Fehler aufgetreten ist
   */
  public T post(Environment env, String path, Map<String, String> headers, byte[] data) throws IOException
  {
    try (InputStream stream = postInternal(env, path, headers, data))
    {
      return MAPPER.readValue(stream, responseType);
    }
  }

  /**
   * DELETE-Request an eine Schnittstelle senden. Die URL der Schnittstelle setzt sich aus der URL des
   * Environments und dem Pfad der API zusammen. Es sind nur HTTP oder HTTPS-URLs möglich.
   *
   * @param env Informationen über den Server der Schnittstelle
   * @param path Pfad der API
   * @param headers HTTP-Header
   * @param data Daten im Body des Request
   *
   * @return Rückgabewert des Servers
   * @throws IOException Wenn beim Aufrufen der Schnittstelle oder beim Bearbeiten der Anfrage beim Server ein
   * Fehler aufgetreten ist
   */
  public T delete(
      Environment env, String path, Map<String, String> headers, byte[] data) throws IOException
  {
    try (InputStream stream = deleteInternal(env, path, headers, data))
    {
      return MAPPER.readValue(stream, responseType);
    }
  }

  /**
   * DELETE-Request an eine Schnittstelle senden. Die URL der Schnittstelle setzt sich aus der URL des
   * Environments und dem Pfad der API zusammen. Es sind nur HTTP oder HTTPS-URLs möglich.
   *
   * @param env Informationen über den Server der Schnittstelle
   * @param path Pfad der API
   * @param headers HTTP-Header
   *
   * @return Rückgabewert des Servers
   * @throws IOException Wenn beim Aufrufen der Schnittstelle oder beim Bearbeiten der Anfrage beim Server ein
   * Fehler aufgetreten ist
   */
  public T delete(Environment env, String path, Map<String, String> headers) throws IOException
  {
    try (InputStream stream = deleteInternal(env, path, headers))
    {
      return MAPPER.readValue(stream, responseType);
    }
  }

  private InputStream getInternal(Environment env, String path, Map<String, String> headers)
      throws IOException
  {
    try
    {
      HttpURLConnection http = createHttpURLConnectionForGetRequest(env, path, headers);

      if (http.getResponseCode() != 200)
      {
        try (InputStream inputStream = http.getErrorStream())
        {
          String response = convertToStringUsingServerResponseType(inputStream);
          throw new RuntimeException(createErrorMessage(http, response));
        }
      }

      return http.getInputStream();
    }
    catch (MalformedURLException e)
    {
      throw new RuntimeException("URL ist nicht erreichbar", e);
    }
    catch (ProtocolException e)
    {
      throw new RuntimeException("Protokoll-Fehler", e);
    }
  }

  private InputStream postInternal(Environment env, String path, Map<String, String> headers, byte[] data)
      throws IOException
  {
    try
    {
      HttpURLConnection http = createHttpURLConnectionForPostRequest(env, path, headers, data);
      sendData(data, http);

      if (http.getResponseCode() != 200)
      {
        try (InputStream inputStream = http.getErrorStream())
        {
          String response = convertToStringUsingServerResponseType(inputStream);
          throw new RuntimeException(createErrorMessage(http, response));
        }
      }

      return http.getInputStream();
    }
    catch (MalformedURLException e)
    {
      throw new RuntimeException("URL ist nicht erreichbar", e);
    }
    catch (ProtocolException e)
    {
      throw new RuntimeException("Protokoll-Fehler", e);
    }
  }

  private InputStream deleteInternal(Environment env, String path, Map<String, String> headers)
      throws IOException
  {
    return deleteInternal(env, path, headers, new byte[0]);
  }

  private InputStream deleteInternal(Environment env, String path, Map<String, String> headers, byte[] data)
      throws IOException
  {
    try
    {
      HttpURLConnection http = createHttpURLConnectionForDeleteRequest(env, path, headers, data);
      if (data.length > 0)
      {
        sendData(data, http);
      }

      if (http.getResponseCode() != 200)
      {
        try (InputStream inputStream = http.getErrorStream())
        {
          String response = convertToStringUsingServerResponseType(inputStream);
          throw new RuntimeException(createErrorMessage(http, response));
        }
      }

      return http.getInputStream();
    }
    catch (MalformedURLException e)
    {
      throw new RuntimeException("URL ist nicht erreichbar", e);
    }
    catch (ProtocolException e)
    {
      throw new RuntimeException("Protokoll-Fehler", e);
    }
  }

  @SuppressFBWarnings("URLCONNECTION_SSRF_FD")
  private HttpURLConnection createHttpURLConnectionForGetRequest(Environment env, String path,
      Map<String, String> headers) throws IOException
  {
    String serverUrl = env.getUrl() + path;

    log.info("Sende GET-Request an {}", serverUrl);
    URL url = new URL(serverUrl);

    if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https"))
    {
      throw new RuntimeException("URL " + serverUrl + " muss das Protokoll HTTP oder HTTPS haben");
    }

    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    setConnectionParametersForGetRequest(env, headers, http);

    return http;
  }

  @SuppressFBWarnings("URLCONNECTION_SSRF_FD")
  private HttpURLConnection createHttpURLConnectionForPostRequest(Environment env, String path,
      Map<String, String> headers, byte[] data) throws IOException
  {
    String serverUrl = env.getUrl() + path;

    log.info("Sende POST-Request an {}", serverUrl);
    URL url = new URL(serverUrl);

    if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https"))
    {
      throw new RuntimeException("URL " + serverUrl + " muss das Protokoll HTTP oder HTTPS haben");
    }

    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    setConnectionParametersForPostRequest(env, data.length, headers, http);

    return http;
  }

  @SuppressFBWarnings("URLCONNECTION_SSRF_FD")
  private HttpURLConnection createHttpURLConnectionForDeleteRequest(Environment env, String path,
      Map<String, String> headers, byte[] data) throws IOException
  {
    String serverUrl = env.getUrl() + path;

    log.info("Sende DELETE-Request an {}", serverUrl);
    URL url = new URL(serverUrl);

    if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https"))
    {
      throw new RuntimeException("URL " + serverUrl + " muss das Protokoll HTTP oder HTTP haben");
    }

    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    setConnectionParametersForDeleteRequest(env, data.length, headers, http);

    return http;
  }

  private void setConnectionParametersForGetRequest(Environment env, Map<String, String> headers,
      HttpURLConnection http) throws ProtocolException
  {
    http.setDoOutput(false);
    http.setRequestMethod("GET");

    // Authorisierungsdaten
    http.setRequestProperty(HTTPHeaderKeys.AUTHORIZATION, getBasicAuthToken(env));

    // Weitere Header
    Optional.ofNullable(headers).ifPresent(h -> h.forEach(http::setRequestProperty));
  }

  private void setConnectionParametersForPostRequest(Environment env, int length, Map<String, String> headers,
      HttpURLConnection http) throws ProtocolException
  {
    http.setFixedLengthStreamingMode(length);
    http.setDoOutput(true);
    http.setRequestMethod("POST");

    // Authorisierungsdaten
    http.setRequestProperty(HTTPHeaderKeys.AUTHORIZATION, getBasicAuthToken(env));

    // Weitere Header
    Optional.ofNullable(headers).ifPresent(h -> h.forEach(http::setRequestProperty));
  }

  private void setConnectionParametersForDeleteRequest(Environment env, int bodyLength, Map<String, String> headers,
      HttpURLConnection http) throws ProtocolException
  {
    allowTransferRequestBodyWhenNonEmptyBody(http, bodyLength);
    http.setRequestMethod("DELETE");

    // Authorisierungsdaten
    http.setRequestProperty(HTTPHeaderKeys.AUTHORIZATION, getBasicAuthToken(env));

    // Weitere Header
    Optional.ofNullable(headers).ifPresent(h -> h.forEach(http::setRequestProperty));
  }

  private static void allowTransferRequestBodyWhenNonEmptyBody(HttpURLConnection http, int bodyLength)
  {
    if (bodyLength > 0)
    {
      http.setFixedLengthStreamingMode(bodyLength);
      http.setDoOutput(true);
    }
    else
    {
      http.setDoOutput(false);
    }
  }

  private String getBasicAuthToken(Environment env)
  {
    String tmp = env.getUser() + ':' + env.getPassword();
    String encoding = Base64.getEncoder().encodeToString(tmp.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encoding;
  }

  private void sendData(byte[] data, HttpURLConnection http)
  {
    try (OutputStream os = http.getOutputStream())
    {
      http.connect();
      os.write(data);
      os.flush();
    }
    catch (Exception e)
    {
      http.disconnect();
      throw createRuntimeException(http, e);
    }
  }

  private String convertToStringUsingServerResponseType(InputStream inputStream) throws IOException
  {
    if (inputStream == null)
    {
      return "";
    }

    String responseAsString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    try
    {
      T value = MAPPER.readValue(responseAsString, responseType);
      return value.toString();
    }
    catch (IOException e)
    {
      return responseAsString;
    }
  }

  private RuntimeException createRuntimeException(HttpURLConnection http, Exception e)
  {
    try
    {
      String message = createErrorMessage(http);
      return new RuntimeException("Fehler in der HTTP-Verbindung: " + message, e);
    }
    catch (Throwable t)
    {
      return new RuntimeException("Fehler in der HTTP-Verbindung", e);
    }
  }

  private String createErrorMessage(HttpURLConnection http) throws IOException
  {
    try (InputStream inputStream = http.getErrorStream())
    {
      String message = convertToStringWithEmptyDefault(inputStream);
      return createErrorMessage(http, message);
    }
  }

  private String createErrorMessage(HttpURLConnection http, String message) throws IOException
  {
    StringBuilder builder = new StringBuilder();
    builder.append("HTTP-Response-Code: ").append(http.getResponseCode());

    if (StringUtils.isNotEmpty(http.getResponseMessage()))
    {
      builder.append(' ').append(http.getResponseMessage());
    }
    if (StringUtils.isNotEmpty(message))
    {
      builder.append(" | Meldung des Servers: ").append(message);
    }

    return builder.append(" | URL: ").append(http.getURL()).toString();
  }

  private String convertToStringWithEmptyDefault(InputStream inputStream) throws IOException
  {
    return inputStream == null ? "" : IOUtils.toString(inputStream, StandardCharsets.UTF_8);
  }
}
