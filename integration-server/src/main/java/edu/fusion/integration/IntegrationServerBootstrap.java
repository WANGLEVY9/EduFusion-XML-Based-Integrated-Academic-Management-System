package edu.fusion.integration;

import edu.fusion.common.model.Result;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.util.XmlUtil;
import edu.fusion.integration.http.IntegrationXmlHttpServer;
import edu.fusion.integration.service.IntegrationServer;
import edu.fusion.servera.service.CollegeAGateway;
import edu.fusion.serverb.service.CollegeBGateway;
import edu.fusion.serverc.service.CollegeCGateway;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.nio.file.Paths;
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

        boolean runDemo = Boolean.parseBoolean(System.getProperty("integration.demo", "false"));
        if (runDemo) {
            Path request = Paths.get("xml", "request-share-a.xml");
            Path requestXsd = Paths.get("xsd", "request.xsd");
            Path responsePath = Paths.get("xml", "response-share-a.xml");

            Result<Document> result = server.processRequestXml(request, requestXsd);
            if (result.isSuccess() && result.getData() != null) {
                XmlUtil.write(result.getData(), responsePath);
                System.out.println("Response XML generated: " + responsePath.toAbsolutePath());
            } else {
                System.out.println("Request process failed: " + result.getMessage());
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(httpServer::stop));
    }
}
