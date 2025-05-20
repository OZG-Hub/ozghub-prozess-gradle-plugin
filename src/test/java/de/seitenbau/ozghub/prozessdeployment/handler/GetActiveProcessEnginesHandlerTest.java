package de.seitenbau.ozghub.prozessdeployment.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import de.seitenbau.ozghub.prozessdeployment.common.Environment;
import static de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys.DEPLOYMENT_ID;
import static de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys.PROCESS_DUPLICATION;
import static de.seitenbau.ozghub.prozessdeployment.common.HTTPHeaderKeys.PROCESS_ENGINE;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpHandler;
import de.seitenbau.ozghub.prozessdeployment.integrationtest.HttpServerFactory;
import de.seitenbau.ozghub.prozessdeployment.model.response.Aggregated;
import de.seitenbau.ozghub.prozessdeployment.model.response.ProcessEngine;
import de.seitenbau.ozghub.prozessdeployment.util.HttpRequestAsserter;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GetActiveProcessEnginesHandlerTest extends BaseTestHandler
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static final String TASK_NAME = "getActiveProcessEngines";

  private HttpServer httpServer = null;

  private GetActiveProcessEnginesOzgHandler sut;

  @Override
  protected File getTestFolder()
  {
    return null;
  }

  @AfterEach
  public void after()
  {
    if (httpServer != null)
    {
      httpServer.stop(0);
    }
  }

  @Test
  public void getActiveProcessEngines_success_noEngines() throws Exception
  {
    //arrange
    byte[] response = constructResponseObject(0);
    HttpHandler httpHandler = new HttpHandler(200, response);
    httpServer = HttpServerFactory.createAndStartHttpServer("/prozess/engine/aggregated", httpHandler);

    Environment env = createEnvironment();

    sut = new GetActiveProcessEnginesOzgHandler(env);

    //act
    sut.list(TASK_NAME);

    //assert
    assertThat(httpHandler.getRequestCount()).isEqualTo(1);
    HttpHandler.Request request = httpHandler.getRequest();
    HttpRequestAsserter headerValueAsserter = new HttpRequestAsserter(request);
    headerValueAsserter.assertContentTypeEquals("application/json");
    headerValueAsserter.assertAcceptEquals("application/json");
    headerValueAsserter.assertAuthHeaderIsSet();
    assertUnnecessaryHeadersNotSet(headerValueAsserter);
  }

  private static void assertUnnecessaryHeadersNotSet(HttpRequestAsserter headerValueAsserter)
  {
    headerValueAsserter.assertHeaderIsNull(DEPLOYMENT_ID);
    headerValueAsserter.assertHeaderIsNull(PROCESS_DUPLICATION);
    headerValueAsserter.assertHeaderIsNull(PROCESS_ENGINE);
  }

  private Environment createEnvironment()
  {
    return new Environment(getUrl(), "foo1", "bar1");
  }

  private String getUrl()
  {
    return "http://localhost:" + httpServer.getAddress().getPort();
  }

  @Test
  public void getActiveProcessEngines_success_singleEngine() throws Exception
  {
    //arrange
    byte[] response = constructResponseObject(1);
    HttpHandler httpHandler = new HttpHandler(200, response);
    httpServer = HttpServerFactory.createAndStartHttpServer("/prozess/engine/aggregated", httpHandler);

    Environment env = createEnvironment();

    sut = new GetActiveProcessEnginesOzgHandler(env);

    //act
    sut.list(TASK_NAME);

    //assert
    assertThat(httpHandler.getRequestCount()).isEqualTo(1);

    HttpHandler.Request request = httpHandler.getRequest();
    HttpRequestAsserter headerValueAsserter = new HttpRequestAsserter(request);
    headerValueAsserter.assertContentTypeEquals("application/json");
    headerValueAsserter.assertAcceptEquals("application/json");
    headerValueAsserter.assertAuthHeaderIsSet();
    assertUnnecessaryHeadersNotSet(headerValueAsserter);
  }

  @Test
  public void getActiveProcessEngines_success_multipleEngines() throws Exception
  {
    //arrange
    byte[] response = constructResponseObject(5);
    HttpHandler httpHandler = new HttpHandler(200, response);
    httpServer = HttpServerFactory.createAndStartHttpServer("/prozess/engine/aggregated", httpHandler);

    Environment env = createEnvironment();

    sut = new GetActiveProcessEnginesOzgHandler(env);

    //act
    sut.list(TASK_NAME);

    //assert
    assertThat(httpHandler.getRequestCount()).isEqualTo(1);
    HttpHandler.Request request = httpHandler.getRequest();
    HttpRequestAsserter headerValueAsserter = new HttpRequestAsserter(request);
    headerValueAsserter.assertContentTypeEquals("application/json");
    headerValueAsserter.assertAcceptEquals("application/json");
    headerValueAsserter.assertAuthHeaderIsSet();
    assertUnnecessaryHeadersNotSet(headerValueAsserter);
  }

  private byte[] constructResponseObject(int amount) throws IOException
  {
    List<ProcessEngine> result = new ArrayList<>();

    if (amount == 1)
    {
      result.add(new ProcessEngine("defaultEngine", "Standard-Prozess-Engine"));
    }
    else if (amount == 5)
    {
      result.add(new ProcessEngine("defaultEngine", "Standard-Prozess-Engine"));
      result.add(new ProcessEngine("engine2", "2. Engine"));
      result.add(new ProcessEngine("process-engine-3", "Dritte Engine"));
      result.add(new ProcessEngine("support-engine", "Support Engine"));
      result.add(new ProcessEngine("prozessengine", "Neue Prozess-Engine"));
    }

    Aggregated<List<ProcessEngine>> aggregatedResult = Aggregated.complete(result);
    return MAPPER.writeValueAsBytes(aggregatedResult);
  }
}
