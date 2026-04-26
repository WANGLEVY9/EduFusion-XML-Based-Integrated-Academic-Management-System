package edu.fusion.integration.http;

import edu.fusion.common.model.Result;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.util.Dom4jXmlService;
import edu.fusion.integration.service.IntegrationServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IntegrationXmlHttpServer {

    private final IntegrationServer integrationServer;
    private HttpServer server;

    public IntegrationXmlHttpServer(List<CollegeGateway> gateways) {
        this.integrationServer = new IntegrationServer(gateways);
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/xml", new XmlHandler());
            server.createContext("/api/health", new HealthHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start integration XML HTTP server", ex);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private final class XmlHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeResponse(exchange, 405, "Only POST is supported");
                return;
            }
            String requestXml = readBody(exchange.getRequestBody());
            Result<Document> result = integrationServer.processRequestXml(requestXml);
            if (!result.isSuccess() || result.getData() == null) {
                Document response = Dom4jXmlService.createDocument("response");
                Element root = response.getRootElement();
                root.addElement("success").addText("false");
                root.addElement("message").addText(result.getMessage());
                writeXml(exchange, 200, Dom4jXmlService.toCompactString(response));
                return;
            }
            writeXml(exchange, 200, Dom4jXmlService.toCompactString(result.getData()));
        }
    }

    private final class HealthHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            writeResponse(exchange, 200, "OK");
        }
    }

    private String readBody(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        StringBuilder builder = new StringBuilder();
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private void writeXml(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        try {
            outputStream.write(bytes);
        } finally {
            outputStream.close();
        }
    }

    private void writeResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        try {
            outputStream.write(bytes);
        } finally {
            outputStream.close();
        }
    }
}
