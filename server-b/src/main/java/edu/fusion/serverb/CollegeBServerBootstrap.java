package edu.fusion.serverb;

import edu.fusion.common.server.CollegeXmlHttpServer;
import edu.fusion.serverb.service.CollegeBGateway;

public class CollegeBServerBootstrap {

    public static final int DEFAULT_PORT = 8082;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        CollegeXmlHttpServer server = new CollegeXmlHttpServer(new CollegeBGateway(), port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
