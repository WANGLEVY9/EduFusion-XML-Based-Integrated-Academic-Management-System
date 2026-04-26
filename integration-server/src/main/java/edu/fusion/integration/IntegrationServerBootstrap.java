package edu.fusion.integration;

import edu.fusion.common.model.Result;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.util.Dom4jXmlService;
import edu.fusion.integration.http.IntegrationXmlHttpServer;
import edu.fusion.integration.service.IntegrationServer;
import edu.fusion.servera.service.CollegeAGateway;
import edu.fusion.serverb.service.CollegeBGateway;
import edu.fusion.serverc.service.CollegeCGateway;
import org.dom4j.Document;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class IntegrationServerBootstrap {

    public static void main(String[] args) {
        List<CollegeGateway> gateways = Arrays.asList(
                new CollegeAGateway(),
                new CollegeBGateway(),
                new CollegeCGateway()
        );

        IntegrationServer server = new IntegrationServer(gateways);
        IntegrationXmlHttpServer httpServer = new IntegrationXmlHttpServer(gateways);
        httpServer.start(8080);
        System.out.println("Integration XML HTTP server started at http://localhost:8080/api/xml");
        System.out.println("XSD validation enabled for /api/xml endpoint");

        boolean runDemo = Boolean.parseBoolean(System.getProperty("integration.demo", "false"));
        if (runDemo) {
            Path request = Path.of("xml", "request-share-a.xml");
            Path requestXsd = Path.of("xsd", "request.xsd");
            Path responsePath = Path.of("xml", "response-share-a.xml");

            Result<Document> result = server.processRequestXml(request, requestXsd);
            if (result.isSuccess() && result.getData() != null) {
                Dom4jXmlService.transform(
                        request,
                        Path.of("xslt", "college-a-to-unified.xslt"),
                        responsePath);
                System.out.println("XSLT transformed response XML generated: " + responsePath.toAbsolutePath());
            } else {
                System.out.println("Request process failed: " + result.getMessage());
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(httpServer::stop));
    }
}
