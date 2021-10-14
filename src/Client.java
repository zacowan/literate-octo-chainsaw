import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client implements Runnable {
  Socket requestSocket; // socket connect to the server
  ObjectOutputStream out; // stream write to the socket
  ObjectInputStream in; // stream read from the socket

  PeerInfo hostInfo;
  PeerInfo targetInfo;

  public Client(PeerInfo hostInfo, PeerInfo targetInfo) {
    this.hostInfo = hostInfo;
    this.targetInfo = targetInfo;
  }

  public void run() {
    try {
      // create a socket to connect to the server
      requestSocket = new Socket(targetInfo.hostname, Integer.parseInt(targetInfo.port));
      System.out.printf("Connected to peer %s at %s:%s.\n", targetInfo.peerID, targetInfo.hostname, targetInfo.port);
      // initialize inputStream and outputStream
      out = new ObjectOutputStream(requestSocket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(requestSocket.getInputStream());

      MessageHandler msgHandler = new MessageHandler();

      // Perform handshake
      msgHandler.sendHandshake(out, hostInfo.peerID);
      msgHandler.receiveHandshake(in, hostInfo.peerID);

    } catch (ConnectException e) {
      System.err.println("Connection refused. You need to initiate a server first.");
    } catch (UnknownHostException unknownHost) {
      System.err.println("You are trying to connect to an unknown host!");
    } catch (IOException ioException) {
      ioException.printStackTrace();
    } finally {
      // Close connections
      try {
        in.close();
        out.close();
        requestSocket.close();
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }
  }

  // send a message to the output stream
  void sendMessage(String msg) {
    try {
      // stream write the message
      out.writeObject(msg);
      out.flush();
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }
}
