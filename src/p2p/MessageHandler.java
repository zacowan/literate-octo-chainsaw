package p2p;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class MessageHandler {
  private String hostID;
  private String hostIP;
  private String hostPort;

  public static enum MessageTypeEnum {
    CHOKE, UNCHOKE, INTERESTED, NOT_INTERESTED, HAVE, BITFIELD, REQUEST, PIECE
  }

  public MessageHandler(String hostID, String hostIP, String hostPort) {
    this.hostID = hostID;
    this.hostIP = hostIP;
    this.hostPort = hostPort;
  }

  public void sendHandshake(ObjectOutputStream out, String peerID) {
    String message = "P2PFILESHARINGPROJ0000000000" + peerID;
    try {
      out.writeObject(message);
      out.flush();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

  }

  public void sendMessage() {

  }
}
