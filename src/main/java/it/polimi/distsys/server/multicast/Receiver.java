package it.polimi.distsys.server.multicast;

import io.reactivex.Observable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Receiver class implements a TCP multicast receiver that
 * returns an Observable which is a stream of received messages.
 * Observers that observe this Observable will see an events for each
 * message received on TCP multicast.
 */
public class Receiver {

    private static final Logger LOGGER = Logger.getLogger(Receiver.class.getName());

    private MulticastSocket socket = null;
    private String address = "230.0.0.0";   // Multicast address
    /**
     * The port is randomized (range 4000-5000) so that we can run
     * multiple instances on different ports for testing purposes
     */
    private int port = getRandomPort();
    private byte[] buf = new byte[256];

    /**
     *  This object is a custom Observable created using Observable.create
     *  which connects to the TCP socket for multicast and creates an observable flow of incoming messages
     */
    private Observable<String> observable =
            Observable.create( subscriber -> {
                // Create a multicast socket on the specified port
                try {
                    socket = new MulticastSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Join a publish group defined by the address
                InetAddress group = null;
                try {
                    group = InetAddress.getByName(address);
                    socket.joinGroup(group);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Enter the receive loop. Each packet will trigger an event to all subscribers (subscriber.onNext())
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                    String received = new String(
                            packet.getData(), 0, packet.getLength());
                    if ("end".equals(received)) {
                        subscriber.onComplete();
                        break;
                    }
                    else {
                        subscriber.onNext(received);
                    }
                }
                try {
                    socket.leaveGroup(group);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket.close();
            });

    /**
     *
     * Public method that subscribers can use to obtain the Observable flow of packets
     * @return Observable
     */
    public Observable<String> getData() {
        return this.observable;
    }

    public int getPort() {
        return port;
    }


    private int getRandomPort() {
        int leftLimit = 4000;
        int rightLimit = 5000;
        int generatedInteger = leftLimit + (int) (new Random().nextFloat() * (rightLimit - leftLimit));
        return generatedInteger;
    }

}
