package p2p;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.Socket;

public class peerProcess {

  public static void main(String args[]) {
    // args[0] = peerID
    String peerID = args[0];

    // TODO: use me
    String numberOfPreferredNeighbors;
    String unchokingInterval;
    String optimisticUnchokingInterval;
    String fileName;
    String fileSize;
    String pieceSize;

    // read Common.cfg
    File commonFile = new File("Common.cfg");
    try {
      Scanner scanner = new Scanner(commonFile);
      numberOfPreferredNeighbors = scanner.nextLine().split(" ")[1];
      unchokingInterval = scanner.nextLine().split(" ")[1];
      optimisticUnchokingInterval = scanner.nextLine().split(" ")[1];
      fileName = scanner.nextLine().split(" ")[1];
      fileSize = scanner.nextLine().split(" ")[1];
      pieceSize = scanner.nextLine().split(" ")[1];
      scanner.close();
    } catch (Exception e) {
      System.out.printf("Error reading Common.cfg: %s", e.getMessage());
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
        if (currentPeer.peerID == peerID) {
          SynchronizedPeerInfoList.instance.setThisPeerIndex(index);
        }
        index++;
      }
      scanner.close();
    } catch (Exception e) {
      System.out.printf("Error reading PeerInfo.cfg: %s", e.getMessage());
    }

    final int thisPeerIndex = SynchronizedPeerInfoList.instance.getThisPeerIndex();

    // spawn server
    try {
      // TODO: Joel
      // server should have access to peerList
      // server should run in its own thread
      // new Server().run(Integer.parseInt(thisPeer.port));
      // new Thread(new Send(server)).start();
      // new Thread(new Recieve(server)).start();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    // spawn X clients
    for (int i = 0; i < SynchronizedPeerInfoList.instance.getSize(); i++) {
      PeerInfo currentPeer = SynchronizedPeerInfoList.instance.getPeer(i);
      if (i != thisPeerIndex) {
        new Client(SynchronizedPeerInfoList.instance.getThisPeer(), currentPeer).start();
      } else {
        break;
      }
    }

    // peerList of all peers
    // while (peers.notHaveFile) wait
    // when (peers.haveFile) terminate
  }

  // set parameters
  // start server
  // connect clients to other peers if there
}
