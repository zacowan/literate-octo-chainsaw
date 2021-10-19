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

  MessageHandler msgHandler;

  public Client(PeerInfo hostInfo, PeerInfo targetInfo) {
    this.hostInfo = hostInfo;
    this.targetInfo = targetInfo;
    this.msgHandler = new MessageHandler();
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

      // Perform handshake
      msgHandler.sendHandshake(out, hostInfo.peerID);
      boolean checkHandshake = msgHandler.receiveHandshakeClient(in, targetInfo.peerID);

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
            handleBitfieldReceived(received);
            break;
          case CHOKE:
            // TODO
            break;
          case UNCHOKE:
            // TODO
            break;
          case PIECE:
            handlePieceReceived(received);
            break;
          default:
            DebugLogger.instance.log("Default case");
            break;
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

  private void handleBitfieldReceived(Message received) {
    // TODO: inspect bitfield, compare with what host needs
    boolean interested = true;
    if (interested) {
      msgHandler.sendMessage(out, 0, MessageType.INTERESTED, null);
    } else {
      msgHandler.sendMessage(out, 0, MessageType.NOT_INTERESTED, null);
    }
  }

  private void handlePieceReceived(Message received) {
    // Store piece in data structure
    PiecePayload piecePayload = new PiecePayload(received.payload);
    PieceStorage.instance.setPiece(piecePayload.index, piecePayload.data);
    // TODO: Update bitfield
    // Send "have" message
    // TODO: change null to bitfield
    msgHandler.sendMessage(out, 0, MessageType.HAVE, null);
    msgHandler.receiveMessage(in);
    // Send "request" message?
    // TODO: determine index based on inspecting bitfield
    RequestPayload requestPayload = new RequestPayload(0);
    msgHandler.sendMessage(out, requestPayload.getLength(), MessageType.REQUEST, requestPayload.getBytes());
  }
}
