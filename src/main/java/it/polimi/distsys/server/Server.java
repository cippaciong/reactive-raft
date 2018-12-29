package it.polimi.distsys.server;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import it.polimi.distsys.server.multicast.Receiver;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class Server {

    private  final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final UUID id = UUID.randomUUID();
    private Receiver receiver = new Receiver();
    private long timeout = randomSeconds();
    Observable<String> messages = receiver.getData();
    ConnectableObservable<String> obs = messages.publish();


    public void addEntryHandler() {
        // This subscriber uses a timeout operator to signal an error if it doesn't receive
        // a message (event) for a certain amount of time (initialized randomly)
        obs.subscribeOn(Schedulers.io())
                .timeout((msg -> {
                            long newTimeout = randomSeconds();
                            this.timeout = newTimeout;
                            return ConnectableObservable.timer(newTimeout, TimeUnit.SECONDS);
                        }), Observable.just("Timeout, reached (" + this.timeout + " seconds), start new election"))
                .filter(msg -> isAddEntry(msg))
                .map(msg -> handleAddEntry(msg))
                .subscribe(
                        msg -> System.out.println("Server " + id + " received an Add Entry message: " + msg),
                        error -> System.out.println("Error")
                );

        LOGGER.info("HERE addentry");
    }

    public void voteRequestHandler() {
        obs.subscribeOn(Schedulers.io())
                .filter(msg -> isVoteRequest(msg))
                .subscribe(
                        msg -> System.out.println("Server " + id + " received a Vote Request message: " + msg),
                        error -> System.out.println("Error")
                );

    }

    public UUID getID() {
        return this.id;
    }


    /* Private Methods */

    public void connectToObservable() {
        // Sleep for 1 second and then connect to the observable to start handling messages
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.severe("Unable to sleep in connectToObservable");
            e.printStackTrace();
        }
        obs.connect();
    }

    // Set the timeout randomly
    private long randomSeconds() {
        long leftLimit = 5L;
        long rightLimit = 10L;
        long generatedLong = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
        System.out.println("Sleep Time: " + generatedLong);
        return generatedLong;
    }

    private String handleAddEntry(String msg) {
        // TODO
        // Handle a request to add an entry to the state machine
        // Here we can split different action depending on the node role (follower/candidate/leader)
        return msg;
    }

    private void handleVoteRequest(String msg) {
        // TODO
        // Handle a vote request from a candidate
    }

    private Boolean isAddEntry(String msg) {
        String action = msg.split(",")[0];
        System.out.println("isAddEntry got action: " + action);
        return "add_entry".equals(action) || "Timeout".equals(action);
    }


    private Boolean isVoteRequest(String msg) {
        String action = msg.split(",")[0];
        System.out.println("isVoteRequest got action: " + action);
        return "vote_request".equals(action);
    }


}
