import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class MessageHandler {
  public String receiveHandshakeServer(ObjectInputStream in) {
    try {
      byte[] bytes = (byte[]) in.readObject();
      HandshakeMessage received = new HandshakeMessage(bytes);
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
      byte[] bytes = (byte[]) in.readObject();
      HandshakeMessage received = new HandshakeMessage(bytes);
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
      out.writeObject(msg.getBytes());
      out.flush();
    } catch (Exception e) {
      DebugLogger.instance.err(e.getMessage());
    }
  }

  public void sendMessage(ObjectOutputStream out, int length, MessageType type, byte[] payload) {
    Message msg = new Message(length, type, payload);
    try {
      out.writeObject(msg.getBytes());
      out.flush();
    } catch (Exception e) {
      DebugLogger.instance.err(e.getMessage());
    }
  }

  public Message receiveMessage(ObjectInputStream in) {
    try {
      byte[] bytes = (byte[]) in.readObject();
      return new Message(bytes);
    } catch (IOException e) {
      DebugLogger.instance.err(e.getMessage());
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      DebugLogger.instance.err(e.getMessage());
    }
    return null;
  }
}
