package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.*;

/**
 * Singleton class that stores the rates that neighbors have fed this peer data.
 *
 * As client threads download data from other peers, they will update the rate
 * at which they get data from those peers here.
 */
public class RateTracker {
    public static RateTracker instance = null;

    private HashMap<String, Double> amountUploaded;
    private Date intervalStartTime;

    public RateTracker(List<PeerInfo> peers) {
        for (PeerInfo peerInfo : peers) {
            amountUploaded.put(peerInfo.peerID, 0.0);
        }
        intervalStartTime = new Date();
    }

    /**
     *
     * Updates the rate for the current unchoking interval.
     *
     * @param peerID the peer ID that fed this peer data.
     * @param bytes  the amount of bytes received by the peer.
     */
    public synchronized void updateRate(String peerID, double bytes) {
        amountUploaded.put(peerID, amountUploaded.get(peerID) + bytes);
    }

    /**
     *
     * Gets the upload rate by a peer for the current unchoking interval.
     *
     * @param peerID the peer ID of the peer you want the rate of.
     * @return the upload rate of that peer, in bytes per second.
     */
    public synchronized double getRate(String peerID) {
        Date t = new Date();
        double seconds = (t.getTime() - intervalStartTime.getTime()) / 1000;
        Double amntUploaded = amountUploaded.get(peerID);
        if (amntUploaded != null) {
            return amntUploaded / seconds;
        } else {
            return 0.0;
        }
    }

    /**
     *
     * Sets the amount uploaded by each peer to 0. Sets the intervalStartTime to
     * now.
     *
     */
    public synchronized void resetRates() {
        Set<String> keys = amountUploaded.keySet();
        for (String peerID : keys) {
            amountUploaded.put(peerID, 0.0);
        }
        intervalStartTime = new Date();
    }

}
