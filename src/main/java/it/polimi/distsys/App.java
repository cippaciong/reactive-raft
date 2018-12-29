package it.polimi.distsys;

import it.polimi.distsys.server.Server;

import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main( String[] args ) throws InterruptedException {

        Server server = new Server();
        LOGGER.info("Spawned new server with id: " + server.getID());
        server.addEntryHandler();
        server.voteRequestHandler();

        LOGGER.info("Done loading handlers, now we can connect them");
        server.connectToObservable();


        // Required by RxJava otherwise the process will stop after the first message
        while(Boolean.TRUE) {
            Thread.sleep(1000);
        }

        System.out.println("End");
    }

}
