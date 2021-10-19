import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.*;
import main.logging.*;

public class peerProcess {

  public static void main(String args[]) {
    // args[0] = peerID
    String peerID = args[0];

    // Initialize debug logger
    DebugLogger.instance = new DebugLogger(peerID);

    // Initialize logging to file
    FileLogger.instance = new FileLogger(peerID);

    // Initialize common config class
    CommonConfig cc = new CommonConfig();

    // read Common.cfg
    File commonFile = new File("Common.cfg");
    try {
      Scanner scanner = new Scanner(commonFile);
      cc.numberOfPreferredNeighbors = scanner.nextLine().split(" ")[1];
      cc.unchokingInterval = scanner.nextLine().split(" ")[1];
      cc.optimisticUnchokingInterval = scanner.nextLine().split(" ")[1];
      cc.fileName = scanner.nextLine().split(" ")[1];
      cc.fileSize = scanner.nextLine().split(" ")[1];
      cc.pieceSize = scanner.nextLine().split(" ")[1];
      scanner.close();
    } catch (Exception e) {
      DebugLogger.instance.err("Error reading Common.cfg");
    }

    // read PeerInfo.cfg
    PeerInfoList.instance = new PeerInfoList();

    File peerInfoFile = new File("PeerInfo.cfg");
    try {
      Scanner scanner = new Scanner(peerInfoFile);
      int index = 0;
      while (scanner.hasNextLine()) {
        String[] line = scanner.nextLine().split(" ");
        PeerInfo currentPeer = new PeerInfo(line[0], line[1], line[2], line[3]);
        PeerInfoList.instance.addPeer(currentPeer);
        if (currentPeer.peerID.equals(peerID)) {
          PeerInfoList.instance.setThisPeerIndex(index);
        }
        index++;
      }
      scanner.close();
    } catch (Exception e) {
      DebugLogger.instance.err("Error reading PeerInfo.cfg");
    }

    // spawn server
    Thread serverThread;
    try {
      Server sv = new Server(PeerInfoList.instance.getThisPeer());
      serverThread = new Thread(sv);
      serverThread.start();
    } catch (Exception e) {
      DebugLogger.instance.err("Error creating the server thread");
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
          DebugLogger.instance.err("Error creating client %d", i);
        }
      } else {
        break;
      }
    }

    // Initialize piece storage
    PieceStorage.instance = new PieceStorage(cc, PeerInfoList.instance.getThisPeer().hasFile);

    // ConnectedClientsList
    // while (clients are connected)
    // set some timer
    // wait until timer is done
    // Check list of connected clients
    // Determine preferred neighbors + optimistically unchoked neighbor
  }
}
