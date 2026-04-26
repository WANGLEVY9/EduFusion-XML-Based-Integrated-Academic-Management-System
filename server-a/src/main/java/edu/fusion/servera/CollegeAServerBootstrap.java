package edu.fusion.servera;

import edu.fusion.common.server.CollegeXmlHttpServer;
import edu.fusion.servera.service.CollegeAGateway;

public class CollegeAServerBootstrap {

    public static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        CollegeXmlHttpServer server = new CollegeXmlHttpServer(new CollegeAGateway(), port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
