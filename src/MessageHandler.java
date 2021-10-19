import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class MessageHandler {
  public String receiveHandshakeServer(ObjectInputStream in) {
    try {
      HandshakeMessage received = (HandshakeMessage) in.readObject();
      if (received.header.equals(HandshakeMessage.HEADER) && Arrays.equals(received.zeroes, HandshakeMessage.ZEROES)) {
        return Integer.toString(received.peerID);
      } else {
        return null;
      }
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
      HandshakeMessage received = (HandshakeMessage) in.readObject();
      if (received.header.equals(HandshakeMessage.HEADER) && Arrays.equals(received.zeroes, HandshakeMessage.ZEROES)
          && received.peerID == Integer.parseInt(peerID)) {
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
    HandshakeMessage msg = new HandshakeMessage(peerID);
    try {
      out.writeObject(msg);
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
