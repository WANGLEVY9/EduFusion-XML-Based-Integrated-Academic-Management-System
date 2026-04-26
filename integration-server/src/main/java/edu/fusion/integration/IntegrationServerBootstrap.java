package edu.fusion.integration;

import edu.fusion.common.server.CollegeXmlHttpServer;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.service.RemoteCollegeGateway;
import edu.fusion.common.util.Dom4jXmlService;
import edu.fusion.integration.http.IntegrationXmlHttpServer;
import edu.fusion.integration.service.IntegrationServer;
import edu.fusion.servera.service.CollegeAGateway;
import edu.fusion.serverb.service.CollegeBGateway;
import edu.fusion.serverc.service.CollegeCGateway;

import java.util.Arrays;
import java.util.List;

public class IntegrationServerBootstrap {

    private static final int INTEGRATION_PORT = 8080;
    private static final int COLLEGE_A_PORT = 8081;
    private static final int COLLEGE_B_PORT = 8082;
    private static final int COLLEGE_C_PORT = 8083;

    public static void main(String[] args) {
        boolean remote = Boolean.parseBoolean(
                System.getProperty("college.remote", "false"));

        List<CollegeGateway> gateways;
        if (remote) {
            gateways = createRemoteGateways();
            System.out.println("Integration Server: using REMOTE college gateways");
        } else {
            gateways = createLocalInlineGateways();
            System.out.println("Integration Server: using LOCAL inline (direct JDBC) college gateways");
        }

        IntegrationServer server = new IntegrationServer(gateways);
        IntegrationXmlHttpServer httpServer = new IntegrationXmlHttpServer(gateways);
        httpServer.start(INTEGRATION_PORT);

        System.out.println("======================================");
        System.out.println("Integration XML HTTP server started at http://localhost:"
                + INTEGRATION_PORT + "/api/xml");
        System.out.println("XSD validation enabled. Remote mode: " + remote);
        System.out.println("======================================");

        Runtime.getRuntime().addShutdownHook(new Thread(httpServer::stop));
    }

    private static List<CollegeGateway> createRemoteGateways() {
        return Arrays.asList(
                new RemoteCollegeGateway("A", "http://localhost:" + COLLEGE_A_PORT + "/api/xml"),
                new RemoteCollegeGateway("B", "http://localhost:" + COLLEGE_B_PORT + "/api/xml"),
                new RemoteCollegeGateway("C", "http://localhost:" + COLLEGE_C_PORT + "/api/xml")
        );
    }

    private static List<CollegeGateway> createLocalInlineGateways() {
        return Arrays.asList(
                new CollegeAGateway(),
                new CollegeBGateway(),
                new CollegeCGateway()
        );
    }

    public static void startLocalDevEnvironment() {
        System.out.println("======================================");
        System.out.println("Starting EduFusion Local Dev Environment");
        System.out.println("======================================");

        CollegeXmlHttpServer serverA = new CollegeXmlHttpServer(
                new CollegeAGateway(), COLLEGE_A_PORT);
        CollegeXmlHttpServer serverB = new CollegeXmlHttpServer(
                new CollegeBGateway(), COLLEGE_B_PORT);
        CollegeXmlHttpServer serverC = new CollegeXmlHttpServer(
                new CollegeCGateway(), COLLEGE_C_PORT);

        serverA.start();
        serverB.start();
        serverC.start();

        List<CollegeGateway> gateways = createRemoteGateways();
        IntegrationServer integrationServer = new IntegrationServer(gateways);
        IntegrationXmlHttpServer httpServer = new IntegrationXmlHttpServer(gateways);
        httpServer.start(INTEGRATION_PORT);

        System.out.println("======================================");
        System.out.println("Local Dev Environment Ready:");
        System.out.println("  College A: http://localhost:" + COLLEGE_A_PORT + "/api/xml");
        System.out.println("  College B: http://localhost:" + COLLEGE_B_PORT + "/api/xml");
        System.out.println("  College C: http://localhost:" + COLLEGE_C_PORT + "/api/xml");
        System.out.println("  Integration: http://localhost:" + INTEGRATION_PORT + "/api/xml");
        System.out.println("======================================");
    }
}
