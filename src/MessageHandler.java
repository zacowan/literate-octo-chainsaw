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
      DebugLogger.instance.err(e.getMessage());
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      DebugLogger.instance.err(e.getMessage());
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
      DebugLogger.instance.err(e.getMessage());
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      DebugLogger.instance.err(e.getMessage());
    }
    return false;
  }

  public void sendHandshake(ObjectOutputStream out, String peerID) {
    String message = HANDSHAKE_PREFIX + peerID;
    try {
      out.writeObject(message);
      out.flush();
    } catch (Exception e) {
      DebugLogger.instance.err(e.getMessage());
    }
  }

  public void sendMessage(ObjectOutputStream out, Message msg) {
    try {
      out.writeObject(msg);
      out.flush();
    } catch (Exception e) {
      DebugLogger.instance.err(e.getMessage());
    }
  }

  public Message receiveMessage(ObjectInputStream in) {
    try {
      return (Message) in.readObject();
    } catch (IOException e) {
      DebugLogger.instance.err(e.getMessage());
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      DebugLogger.instance.err(e.getMessage());
    }
    return null;
  }
}
