package p2p;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class MessageHandler {
  public static enum MessageType {
    CHOKE((byte) 0), UNCHOKE((byte) 1), INTERESTED((byte) 2), NOT_INTERESTED((byte) 3), HAVE((byte) 4),
    BITFIELD((byte) 5), REQUEST((byte) 6), PIECE((byte) 7);

    public byte value;

    private MessageType(byte value) {
      this.value = value;
    }
  }

  public void sendHandshake(ObjectOutputStream out, int peerID) {
    String message = "P2PFILESHARINGPROJ0000000000" + peerID;
    try {
      out.writeObject(message);
      out.flush();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

  }

  public void sendMessage(ObjectOutputStream socket, MessageType type, Optional<ArrayList<Byte>> payload) {
    String message = "test message";
    try {
      socket.writeObject(message);
      socket.flush();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
