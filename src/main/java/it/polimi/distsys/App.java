package it.polimi.distsys;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import it.polimi.distsys.server.multicast.Receiver;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InterruptedException {

        final Logger LOGGER = Logger.getLogger(App.class.getName());

        Receiver receiver = new Receiver();
        LOGGER.info("TCP Receiver listening on port: " + receiver.getPort());

        Observable<String> messages = receiver.getData();
        ConnectableObservable<String> obs = messages.publish();


        // This subscriber uses a timeout operator to signal an error if it doesn't receive
        // a message (event) for a certain amount of time (initialized randomly)
        obs.subscribeOn(Schedulers.io())
                .timeout((n -> ConnectableObservable.timer(randomSeconds(), TimeUnit.SECONDS)),
                        Observable.just("Fallback"))
                .map(msg -> handleMessage(msg))
                .subscribe(
                        msg -> LOGGER.info("Subscriber 1:"+ receiver.getPort() + " got message " + msg),
                        error -> LOGGER.info(("Timeout reached, start new election"))
                );


        obs.subscribeOn(Schedulers.io())
                .subscribe(msg -> System.out.println("Subscriber 2:"+ receiver.getPort() + " got message " + msg));

        LOGGER.info("Sleeping for 1000ms");
        Thread.sleep(1000);

        LOGGER.info("Connecting both Observers to the ConnectableObservable");
        obs.connect();

        // Required by RxJava otherwise the process will stop after the first message
        while(Boolean.TRUE) {
            Thread.sleep(1000);
        }

        LOGGER.info("End");
    }

    // Set the timeout randomly
    private static long randomSeconds() {
        long leftLimit = 5L;
        long rightLimit = 10L;
        long generatedLong = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
        System.out.println("Sleep Time: " + generatedLong);
        return generatedLong;
    }

    // TODO
    private static String handleMessage(String msg) {
        return msg;
    }
}
