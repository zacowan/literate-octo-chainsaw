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
      DebugLogger.instance.log("Opened socket to %s:%s", targetInfo.hostname, targetInfo.port);

      // initialize inputStream and outputStream
      out = new ObjectOutputStream(requestSocket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(requestSocket.getInputStream());

      // Initialize message handler
      MessageHandler msgHandler = new MessageHandler();

      // Perform handshake
      msgHandler.sendHandshake(out, hostInfo.peerID);
      boolean checkHandshake = msgHandler.receiveHandshakeClient(in, hostInfo.peerID);

      if (checkHandshake) {
        DebugLogger.instance.log("Handshake valid");
        Logger.instance.logTCPConnectionTo(targetInfo.peerID);

        // TODO: send bitfield message

        while (PeerInfoList.instance.getThisPeer().hasFile == false) {
          // Wait for message
          Message received = msgHandler.receiveMessage(in);
          DebugLogger.instance.log("Received %s message", received.type.toString());

          // Handle the received message
          switch (received.type) {
            case BITFIELD:
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
              // TODO
            case UNCHOKE:
              // TODO
            case PIECE:
              // TODO
            default:
              DebugLogger.instance.log("Default case");
          }
        }
      } else {
        DebugLogger.instance.err("Handshake invalid");
      }
    } catch (ConnectException e) {
      DebugLogger.instance.err("Connection refused. You need to initiate a server first.");
    } catch (UnknownHostException unknownHost) {
      DebugLogger.instance.err("You are trying to connect to an unknown host!");
    } catch (IOException ioException) {
      ioException.printStackTrace();
    } finally {
      // Close connections
      try {
        in.close();
        out.close();
        requestSocket.close();
        DebugLogger.instance.log("Successfully closed client for %s", hostInfo.peerID);
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }
  }
}
