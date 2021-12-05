package main;

import java.util.HashMap;

import main.logging.DebugLogger;

public class ThreadManagement {
    public static ThreadManagement instance = null;

    private HashMap<String, Boolean> serverThreads = new HashMap<>();
    private HashMap<String, Boolean> clientThreads = new HashMap<>();

    public synchronized void insertServerThread(String peerID) {
        serverThreads.put(peerID, false);
    }

    public synchronized void setServerThreadReady(String peerID) {
        serverThreads.put(peerID, true);
        DebugLogger.instance.log("Server for peer %s is ready", peerID);
    }

    public synchronized void insertClientThread(String peerID) {
        clientThreads.put(peerID, false);
    }

    public synchronized void setClientThreadReady(String peerID) {
        clientThreads.put(peerID, true);
        DebugLogger.instance.log("Client for peer %s is ready", peerID);
    }

    public synchronized Boolean checkIfSafeToExit() {
        DebugLogger.instance.log("Checking if safe to exit...");
        for (Boolean val : serverThreads.values()) {
            if (!val) {
                return false;
            }
        }

        for (Boolean val : clientThreads.values()) {
            if (!val) {
                return false;
            }
        }

        return true;
    }
}
