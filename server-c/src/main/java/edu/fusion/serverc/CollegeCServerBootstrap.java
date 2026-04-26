package edu.fusion.serverc;

import edu.fusion.common.server.CollegeXmlHttpServer;
import edu.fusion.serverc.service.CollegeCGateway;

public class CollegeCServerBootstrap {

    public static final int DEFAULT_PORT = 8083;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        CollegeXmlHttpServer server = new CollegeXmlHttpServer(new CollegeCGateway(), port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
