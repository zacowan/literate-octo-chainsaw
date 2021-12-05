import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.*;
import main.logging.*;

public class peerProcess {

    private static final String CC_FILENAME = "Common.cfg";
    private static final String PI_FILENAME = "PeerInfo.cfg";

    public static void main(String args[]) {
        // args[0] = peerID
        String peerID = null;
        try {
            peerID = args[0];
        } catch (Exception e) {
            e.printStackTrace();
            System.err.printf("No peerID passed to peerProcess, %s\n", e.getMessage());
            System.exit(-1);
        }

        // Initialize debug logger
        DebugLogger.instance = new DebugLogger(peerID);

        DebugLogger.instance.log("Initializing peerProcess for peer %s...", peerID);

        // Initialize logging to file
        FileLogger.instance = new FileLogger(peerID);

        // read Common.cfg
        File commonFile = new File(CC_FILENAME);
        try {
            Scanner scanner = new Scanner(commonFile);
            CommonConfig.numberOfPreferredNeighbors = scanner.nextLine().split(" ")[1];
            CommonConfig.unchokingInterval = scanner.nextLine().split(" ")[1];
            CommonConfig.optimisticUnchokingInterval = scanner.nextLine().split(" ")[1];
            CommonConfig.fileName = scanner.nextLine().split(" ")[1];
            CommonConfig.fileSize = scanner.nextLine().split(" ")[1];
            CommonConfig.pieceSize = scanner.nextLine().split(" ")[1];
            CommonConfig.numPieces = (int) Math
                    .ceil(Double.parseDouble(CommonConfig.fileSize) / Double.parseDouble(CommonConfig.pieceSize));
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
            DebugLogger.instance.err("Error parsing %s file, %s", CC_FILENAME, e.getMessage());
            System.exit(-1);
        }

        // read PeerInfo.cfg
        PeerInfoList.instance = new PeerInfoList();

        File peerInfoFile = new File(PI_FILENAME);
        try {
            Scanner scanner = new Scanner(peerInfoFile);
            int index = 0;
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" ");
                PeerInfo currentPeer = new PeerInfo(line[0], line[1], line[2], line[3], index);
                PeerInfoList.instance.addPeer(currentPeer);
                if (currentPeer.peerID.equals(peerID)) {
                    PeerInfoList.instance.setThisPeerIndex(index);
                }
                index++;
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
            DebugLogger.instance.err("Error parsing %s file, %s", PI_FILENAME, e.getMessage());
            System.exit(-1);
        }

        // Initialize piece storage
        PieceStorage.instance = new PieceStorage(PeerInfoList.instance.getThisPeer().hasFile);

        // Initialize rate tracker
        RateTracker.instance = new RateTracker(PeerInfoList.instance.getList());

        // spawn server
        Thread serverThread;
        try {
            Server sv = new Server(PeerInfoList.instance.getThisPeer());
            serverThread = new Thread(sv);
            serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            DebugLogger.instance.err("Failed to start the server thread, %s", e.getMessage());
            System.exit(-1);
        }

        // spawn X clients
        ArrayList<Thread> clientThreads = new ArrayList<>();
        final int thisPeerIndex = PeerInfoList.instance.getThisPeerIndex();
        for (int i = 0; i < PeerInfoList.instance.getSize(); i++) {
            PeerInfo currentPeer = PeerInfoList.instance.getPeer(i);
            if (i != thisPeerIndex) {
                try {
                    Client cl = new Client(PeerInfoList.instance.getThisPeer(), currentPeer);
                    Thread th = new Thread(cl);
                    clientThreads.add(th);
                    th.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    DebugLogger.instance.err("Error creating client thread %d, %s", i, e.getMessage());
                    System.exit(-1);
                }
            } else {
                break;
            }
        }

        DebugLogger.instance.log("Finished initializing peerProcess for peer %s", peerID);

        DebugLogger.instance.log("Exited peerProcess initialization thread");
    }
}
