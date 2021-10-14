import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class MessageHandler {
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

  public void sendMessage(ObjectOutputStream out, Message msg) {
    String message = "test message";
    try {
      out.writeObject(message);
      out.flush();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public Message receiveMessage(ObjectInputStream in) {
    try {
      String received = (String) in.readObject();
      return new Message(MessageType.INTERESTED, null);
    } catch (IOException e) {
      System.err.println("[receiveHandshake]: IO exception.");
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.err.println("[receiveHandshake]: class not found.");
    }
    return null;
  }
}
