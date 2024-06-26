package de.seitenbau.ozghub.prozessdeployment.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServerConnectionHelper<T>
{
  private static final ObjectMapper MAPPER = createObjectMapper();

  private static ObjectMapper createObjectMapper()
  {
    ObjectMapper objectMapper = new ObjectMapper();
    // Jackson hat aktuell (19.01.2024) keinen nativen Support für LocalDate, erweitere jackson
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  private final TypeReference<T> responseType;

  public ServerConnectionHelper(TypeReference<T> responseType)
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
  public T get(Environment env, String path, Map<String, String> headers)
      throws IOException
  {
    try (InputStream stream = getInternal(env, path, headers))
    {
      return getResponseObject(stream);
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
  public T post(Environment env, String path, Map<String, String> headers, byte[] data)
      throws IOException
  {
    try (InputStream stream = postInternal(env, path, headers, data))
    {
      return getResponseObject(stream);
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
  public T delete(Environment env, String path, Map<String, String> headers, byte[] data)
      throws IOException
  {
    try (InputStream stream = deleteInternal(env, path, headers, data))
    {
      return getResponseObject(stream);
    }
  }

  private InputStream getInternal(Environment env, String path, Map<String, String> headers)
      throws IOException
  {
    try
    {
      HttpURLConnection http = createHttpURLConnectionForGetRequest(env, path, headers);

      if (isNotSuccessCode(http))
      {
        throw exceptionForErrorResponse(http);
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
      sendRequest(data, http);

      if (isNotSuccessCode(http))
      {
        throw exceptionForErrorResponse(http);
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

  private InputStream deleteInternal(Environment env, String path, Map<String, String> headers, byte[] data)
      throws IOException
  {
    try
    {
      HttpURLConnection http = createHttpURLConnectionForDeleteRequest(env, path, headers, data);
      sendRequest(data, http);

      if (isNotSuccessCode(http))
      {
        throw exceptionForErrorResponse(http);
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

  private static boolean isNotSuccessCode(HttpURLConnection http) throws IOException
  {
    return http.getResponseCode() / 100 != 2;
  }

  @SuppressWarnings("unchecked")
  private T getResponseObject(InputStream responseStream) throws IOException
  {
    if (responseType == null)
    {
      return null;
    }

    // Special handling for String responses.
    if (responseType.getType().getTypeName().equals(String.class.getName()))
    {
      return (T) IOUtils.toString(responseStream, StandardCharsets.UTF_8);
    }

    return MAPPER.readValue(responseStream, responseType);
  }

  @SuppressFBWarnings("URLCONNECTION_SSRF_FD")
  private HttpURLConnection createHttpURLConnectionForGetRequest(Environment env, String path,
      Map<String, String> headers) throws IOException
  {
    String serverUrl = env.url() + path;

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
    String serverUrl = env.url() + path;

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
    String serverUrl = env.url() + path;

    log.info("Sende DELETE-Request an {}", serverUrl);
    URL url = new URL(serverUrl);

    if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https"))
    {
      throw new RuntimeException("URL " + serverUrl + " muss das Protokoll HTTP oder HTTP haben");
    }

    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    setConnectionParametersForDeleteRequest(env, data, headers, http);

    return http;
  }

  private void setConnectionParametersForGetRequest(Environment env, Map<String, String> headers,
      HttpURLConnection http) throws ProtocolException
  {
    http.setDoOutput(false);
    http.setRequestMethod("GET");

    http.setRequestProperty(HTTPHeaderKeys.AUTHORIZATION, getBasicAuthToken(env));

    // Prevent use of cached responses for GET requests
    http.setRequestProperty(HTTPHeaderKeys.CACHE_CONTROL, "no-cache");

    // Weitere Header
    Optional.ofNullable(headers).ifPresent(h -> h.forEach(http::setRequestProperty));
  }

  private void setConnectionParametersForPostRequest(Environment env, int length, Map<String, String> headers,
      HttpURLConnection http) throws ProtocolException
  {
    http.setFixedLengthStreamingMode(length);
    http.setDoOutput(true);
    http.setRequestMethod("POST");

    http.setRequestProperty(HTTPHeaderKeys.AUTHORIZATION, getBasicAuthToken(env));

    // Weitere Header
    Optional.ofNullable(headers).ifPresent(h -> h.forEach(http::setRequestProperty));
  }

  private void setConnectionParametersForDeleteRequest(
      Environment env,
      byte[] data,
      Map<String, String> headers,
      HttpURLConnection http) throws ProtocolException
  {
    allowTransferRequestBodyWhenNonEmptyBody(http, data);
    http.setRequestMethod("DELETE");

    http.setRequestProperty(HTTPHeaderKeys.AUTHORIZATION, getBasicAuthToken(env));

    // Weitere Header
    Optional.ofNullable(headers).ifPresent(h -> h.forEach(http::setRequestProperty));
  }

  private static void allowTransferRequestBodyWhenNonEmptyBody(HttpURLConnection http, byte[] data)
  {
    if (data != null && data.length > 0)
    {
      http.setFixedLengthStreamingMode(data.length);
      http.setDoOutput(true);
    }
    else
    {
      http.setDoOutput(false);
    }
  }

  private String getBasicAuthToken(Environment env)
  {
    String tmp = env.user() + ':' + env.password();
    String encoding = Base64.getEncoder().encodeToString(tmp.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encoding;
  }

  private void sendRequest(byte[] data, HttpURLConnection http)
  {
    try
    {
      http.connect();
    }
    catch (IOException e)
    {
      throw createRuntimeException(http, e);
    }

    if (data != null)
    {
      writeData(data, http);
    }
  }

  private void writeData(byte[] data, HttpURLConnection http)
  {
    try (OutputStream os = http.getOutputStream())
    {
      os.write(data);
      os.flush();
      log.debug("Response Code: " + http.getResponseCode());
    }
    catch (Exception e)
    {
      http.disconnect();
      throw createRuntimeException(http, e);
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

  private RuntimeException exceptionForErrorResponse(HttpURLConnection http) throws IOException
  {
    return new RuntimeException(createErrorMessage(http));
  }

  private String createErrorMessage(HttpURLConnection http) throws IOException
  {
    try (InputStream errorStream = http.getErrorStream())
    {
      String message = convertToString(errorStream);
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

  private String convertToString(InputStream inputStream) throws IOException
  {
    return inputStream == null ? "" : IOUtils.toString(inputStream, StandardCharsets.UTF_8);
  }

  public static String encodeUrl(String value)
  {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
