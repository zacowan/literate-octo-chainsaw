import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class peerProcess {

  public static void main(String args[]) {
    // args[0] = peerID
    String peerID = args[0];

    DebugLogger.instance = new DebugLogger(peerID);

    CommonConfig.instance = new CommonConfig();
    CommonConfig cc = CommonConfig.instance;

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

    SynchronizedPeerInfoList.instance = new SynchronizedPeerInfoList();

    // read PeerInfo.cfg
    File peerInfoFile = new File("PeerInfo.cfg");
    try {
      Scanner scanner = new Scanner(peerInfoFile);
      int index = 0;
      while (scanner.hasNextLine()) {
        String[] line = scanner.nextLine().split(" ");
        PeerInfo currentPeer = new PeerInfo(line[0], line[1], line[2], line[3]);
        SynchronizedPeerInfoList.instance.addPeer(currentPeer);
        if (currentPeer.peerID.equals(peerID)) {
          SynchronizedPeerInfoList.instance.setThisPeerIndex(index);
        }
        index++;
      }
      scanner.close();
    } catch (Exception e) {
      DebugLogger.instance.err("Error reading PeerInfo.cfg");
    }

    final int thisPeerIndex = SynchronizedPeerInfoList.instance.getThisPeerIndex();
    final PeerInfo thisPeer = SynchronizedPeerInfoList.instance.getThisPeer();

    // Create log file
    Logger.instance = new Logger(thisPeer.peerID);

    // spawn server
    try {
      Server sv = new Server(thisPeer);
      Thread th = new Thread(sv);
      th.start();
    } catch (Exception e) {
      DebugLogger.instance.err("Error creating the server thread");
    }
    // spawn X clients
    for (int i = 0; i < SynchronizedPeerInfoList.instance.getSize(); i++) {
      PeerInfo currentPeer = SynchronizedPeerInfoList.instance.getPeer(i);
      if (i != thisPeerIndex) {
        try {
          Client cl = new Client(thisPeer, currentPeer);
          Thread th = new Thread(cl);
          th.start();
        } catch (Exception e) {
          DebugLogger.instance.err("Error creating client %d", i);
        }
      } else {
        break;
      }
    }

    // peerList of all peers
    // while (peers.notHaveFile) wait
    // when (peers.haveFile) terminate

    // Close the log file
    // Logger.instance.closeLogFile();
  }

  // set parameters
  // start server
  // connect clients to other peers if there
}
