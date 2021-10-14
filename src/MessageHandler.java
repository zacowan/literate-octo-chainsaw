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

  private static final String HANDSHAKE_PREFIX = "P2PFILESHARINGPROJ0000000000";

  public String receiveHandshakeServer(ObjectInputStream in) {
    try {
      String received = (String) in.readObject();
      String peerID = received.substring(HANDSHAKE_PREFIX.length());
      return peerID;
    } catch (IOException e) {
      System.err.println("[receiveHandshake]: IO exception.");
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.err.println("[receiveHandshake]: class not found.");
    }
    return null;
  }

  public boolean receiveHandshakeClient(ObjectInputStream in, String peerID) {
    try {
      String received = (String) in.readObject();
      if (received.equals(HANDSHAKE_PREFIX + peerID)) {
        return true;
      } else {
        return false;
      }
    } catch (IOException e) {
      System.err.println("[receiveHandshake]: IO exception.");
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.err.println("[receiveHandshake]: class not found.");
    }
    return false;
  }

  public void sendHandshake(ObjectOutputStream out, String peerID) {
    String message = HANDSHAKE_PREFIX + peerID;
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
