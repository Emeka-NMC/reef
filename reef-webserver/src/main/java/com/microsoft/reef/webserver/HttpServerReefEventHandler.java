/**
 * Copyright (C) 2014 Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.reef.webserver;

import com.microsoft.reef.driver.context.ActiveContext;
import com.microsoft.reef.driver.evaluator.EvaluatorDescriptor;
import com.microsoft.reef.driver.parameters.ClientCloseHandlers;
import com.microsoft.tang.InjectionFuture;
import com.microsoft.tang.annotations.Parameter;
import com.microsoft.wake.EventHandler;
import com.microsoft.wake.time.Clock;
import com.microsoft.wake.time.event.StopTime;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HttpServerReefEventHandler
 */
public final class HttpServerReefEventHandler implements HttpHandler {

  /**
   * Standard Java logger.
   */
  private static final Logger LOG = Logger.getLogger(HttpServerReefEventHandler.class.getName());

  private String uriSpecification = "Reef";

  /**
   * reference of ReefEventStateManager
   */
  private final ReefEventStateManager reefStateManager;

  /**
   * reference of ClientCloseHandlers
   */
  private final Set<EventHandler<Void>> clientCloseHandlers;

  private final InjectionFuture<Set<EventHandler<StopTime>>> runtimeStopHandler;

  /**
   * HttpServerReefEventHandler constructor.
   */
  @Inject
  public HttpServerReefEventHandler(ReefEventStateManager reefStateManager,
                                    final @Parameter(ClientCloseHandlers.class) Set<EventHandler<Void>> clientCloseHandlers,
                                    final @Parameter(Clock.RuntimeStopHandler.class) InjectionFuture<Set<EventHandler<StopTime>>> runtimeStopHandler) {
    this.reefStateManager = reefStateManager;
    this.clientCloseHandlers = clientCloseHandlers;
    this.runtimeStopHandler = runtimeStopHandler;
  }

  /**
   * returns URI specification for the handler
   *
   * @return
   */
  @Override
  public String getUriSpecification() {
    return uriSpecification;
  }

  /**
   * set URI specification
   *
   * @param s
   */
  public void setUriSpecification(final String s) {
    uriSpecification = s;
  }

  /**
   * it is called when receiving a http request
   *
   * @param request
   * @param response
   */
  @Override
  public void onHttpRequest(
      final HttpServletRequest request,
      final HttpServletResponse response) throws IOException, ServletException {

    LOG.log(Level.INFO, "HttpServerReefEventHandler in webserver onHttpRequest is called: {0}",
        request.getRequestURI());

    final ParsedHttpRequest parsedHttpRequest = new ParsedHttpRequest(request);
    final String version =  parsedHttpRequest.getVersion().toLowerCase();
    final String target = parsedHttpRequest.getTargetEntity().toLowerCase();

    switch (target) {
      case "evaluators": {
        final String queryStr = parsedHttpRequest.getQueryString();
        if (queryStr == null || queryStr.isEmpty()) {
          if (version.equals("v1")) {
            writeEvaluatorsJsonOutput(response);
          } else {
            writeEvaluatorsWebOutput(response);
          }
        } else {
          handleQueries(response, parsedHttpRequest.getQueryMap(), version);
        }
        break;
      }
      case "driver":
        writeDriverInformation(response);
        break;
      case "close":
        for (EventHandler<Void> e : clientCloseHandlers) {
          e.onNext(null);
        }
        response.getWriter().println("Enforced closing");
        break;
      case "kill":
        for (final ActiveContext context : reefStateManager.getContexts().values()) {
          context.close();
        }
        response.getWriter().println("Killing");
        break;
      case "logfile":
        final byte[] outputBody = readFile().getBytes(Charset.forName("UTF-8"));
        response.getOutputStream().write(outputBody);
        //response.getWriter().println(readFile());
        break;
      default:
        response.getWriter().println(String.format("Unsupported query for entity: [%s].", target));
    }
  }

  /**
   * handle queries
   * Example of a query: http://localhost:8080/reef/Evaluators/?id=Node-2-1403225213803&id=Node-1-1403225213712
   *
   * @param response
   * @param queries
   * @throws IOException
   */
  private void handleQueries(
      final HttpServletResponse response,
      final Map<String, List<String>> queries,
      final String version) throws IOException {
    LOG.log(Level.INFO, "HttpServerReefEventHandler handleQueries is called");

    for (final Map.Entry<String, List<String>> entry : queries.entrySet()) {
      final String queryTarget = entry.getKey().toLowerCase();

      switch(queryTarget) {
        case "id":
          if (version.equals("v1")) {
            writeEvaluatorInfoJsonOutput(response, entry.getValue());
          } else {
            writeEvaluatorInfoWebOutput(response, entry.getValue());
          }
          break;
        default:
          response.getWriter().println("Not supported query : " + queryTarget);
          break;
      }
    }
  }

  private void writeEvaluatorInfoJsonOutput(HttpServletResponse response, List<String> ids) throws IOException {
    AvroEvaluatorInfoSerializer serializer = new AvroEvaluatorInfoSerializer();
    AvroEvaluatorsInfo evaluatorsInfo = serializer.toAvro(ids, this.reefStateManager.getEvaluators());
    final byte[] outputBody = serializer.toString(evaluatorsInfo).getBytes(Charset.forName("UTF-8"));
    response.getOutputStream().write(outputBody);
  }

  private void writeEvaluatorInfoWebOutput(final HttpServletResponse response, final List<String> ids) throws IOException{
    for (String id : ids)
    {
      final EvaluatorDescriptor evaluatorDescriptor =
          this.reefStateManager.getEvaluators().get(id);
      final PrintWriter writer = response.getWriter();

      if (evaluatorDescriptor != null) {
        final String nodeId = evaluatorDescriptor.getNodeDescriptor().getId();
        final String nodeName = evaluatorDescriptor.getNodeDescriptor().getName();
        final InetSocketAddress address =
            evaluatorDescriptor.getNodeDescriptor().getInetSocketAddress();
        writer.println("Evaluator Id: " + id);
        writer.write("<br/>");
        writer.println("Evaluator Node Id: " + nodeId);
        writer.write("<br/>");
        writer.println("Evaluator Node Name: " + nodeName);
        writer.write("<br/>");
        writer.println("Evaluator InternetAddress: " + address);
        writer.write("<br/>");

      } else {
        writer.println("Incorrect Evaluator Id: " + id);
      }
    }
  }

  /**
   * Get all evaluator ids and send it back to response as Jason format
   *
   * @param response
   * @throws IOException
   */
  private void writeEvaluatorsJsonOutput(final HttpServletResponse response) throws IOException {
    LOG.log(Level.INFO, "HttpServerReefEventHandler getEvaluators is called");

    final AvroEvaluatorListSerializer serializer = new AvroEvaluatorListSerializer();
    final AvroEvaluatorList evaluatorList = serializer.toAvro(reefStateManager.getEvaluators(), reefStateManager.getEvaluators().size(), this.reefStateManager.getStartTime());
    final byte[] outputBody = serializer.toString(evaluatorList).getBytes(Charset.forName("UTF-8"));
    response.getOutputStream().write(outputBody);
  }

  /**
   * Get all evaluator ids and send it back to response that can be displayed on web
   * @param response
   * @throws IOException
   */
  private void writeEvaluatorsWebOutput(final HttpServletResponse response) throws IOException {
    LOG.log(Level.INFO, "HttpServerReefEventHandler getEvaluators is called");

    final PrintWriter writer = response.getWriter();

    writer.println("<h1>Evaluators:</h1>");

    for (final Map.Entry<String, EvaluatorDescriptor> entry
        : this.reefStateManager.getEvaluators().entrySet()) {

      final String key = entry.getKey();
      final EvaluatorDescriptor descriptor = entry.getValue();

      writer.println(
          String.format("Evaluator [%s] with [%s]MB memory is running on [%s].",
              key,
              descriptor.getMemory(),
              descriptor.getNodeDescriptor().getInetSocketAddress()));
      writer.write("<br/>");
    }
    writer.write("<br/>");
    writer.println("Total number of Evaluators: " + this.reefStateManager.getEvaluators().size());
    writer.write("<br/>");
    writer.println(String.format("Driver Start Time:[%s]", this.reefStateManager.getStartTime()));
  }

  /**
   * Get driver information
   *
   * @param response
   * @throws IOException
   */
  private void writeDriverInformation(final HttpServletResponse response) throws IOException {

    LOG.log(Level.INFO, "HttpServerReefEventHandler writeDriverInformation invoked.");

    final PrintWriter writer = response.getWriter();

    writer.println("<h1>Driver Information:</h1>");

    writer.println(String.format("Driver Remote Identifier:[%s]",
        this.reefStateManager.getDriverEndpointIdentifier()));

    writer.write("<br/><br/>");
    writer.println(String.format("Driver Start Time:[%s]", this.reefStateManager.getStartTime()));
  }

  private static String readFile() throws FileNotFoundException, IOException{
//    BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\juwang\\Documents\\GitHub\\REEF\\reef-examples\\REEF_LOCAL_RUNTIME\\HelloREEF-1403153262882\\driver\\stderr.txt"));
    BufferedReader br = new BufferedReader(new FileReader("stderr.txt"));
    StringBuilder sb;
    try {
      sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }
    } finally {
      br.close();
    }
    return sb.toString();
  }
}
