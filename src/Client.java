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
      boolean checkHandshake = msgHandler.receiveHandshakeClient(in, hostInfo.peerID);
      if (checkHandshake) {
        Logger.instance.logTCPConnectionTo(targetInfo.peerID);
        System.out.println("Handshake successful.");

        // TODO: send bitfield message

        while (true) {
          // Wait for message
          Message received = msgHandler.receiveMessage(in);

          // Handle the received message
          switch (received.type) {
            case BITFIELD:
              System.out.println("Received bitfield.");
              // TODO: inspect bitfield, compare with what host needs
              boolean interested = true;
              if (interested) {
                Message toSend = new Message(MessageType.INTERESTED, null);
                msgHandler.sendMessage(out, toSend);
              } else {
                Message toSend = new Message(MessageType.NOT_INTERESTED, null);
                msgHandler.sendMessage(out, toSend);
              }
            case CHOKE:
              System.out.println("Received choke.");
            case UNCHOKE:
              System.out.println("Received unchoke.");
            case PIECE:
              System.out.println("Received piece.");
            default:
              System.out.println("Default case.");
          }
        }
      } else {
        System.err.println("Handshake failed.");
      }
    } catch (ConnectException e) {
      System.err.println("Connection refused. You need to initiate a server first.");
    } catch (UnknownHostException unknownHost) {
      System.err.println("You are trying to connect to an unknown host!");
    } catch (IOException ioException) {
      ioException.printStackTrace();
    } finally {
      // Close connections
      try {
        Logger.instance.closeLogFile();
        in.close();
        out.close();
        requestSocket.close();
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }
  }
}
