package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.*;
import main.messaging.*;
import main.messaging.payloads.*;

public class Client implements Runnable {
  Socket requestSocket; // socket connect to the server
  ObjectOutputStream out; // stream write to the socket
  ObjectInputStream in; // stream read from the socket

  PeerInfo hostInfo;
  PeerInfo targetInfo;
  private HashMap<Integer, PeerInfo> peerMap;

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
        FileLogger.instance.logTCPConnectionTo(targetInfo.peerID);

        // Send bitfield message
        msgHandler.sendMessage(out, MessageType.BITFIELD, new BitfieldPayload(hostInfo.bitfield));

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
            handleChokeReceived(received);
            break;
          case UNCHOKE:
            handleUnchokeReceived(received);
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
    // Store bitfield in list of peers
    BitfieldPayload payload = (BitfieldPayload) received.getPayload();
    PeerInfoList.instance.updatePeerBitfield(PeerInfoList.instance.getPeerIndex(targetInfo.peerID), payload.bitfield);
    // TODO: inspect bitfield, compare with what host needs
    boolean interested = true;
    if (interested) {
      msgHandler.sendMessage(out, MessageType.INTERESTED, new EmptyPayload());
    } else {
      msgHandler.sendMessage(out, MessageType.NOT_INTERESTED, new EmptyPayload());
    }
  }

  private void handlePieceReceived(Message received) {
    // Store piece in data structure
    PiecePayload payload = (PiecePayload) received.getPayload();
    PieceStorage.instance.setPiece(payload.index, payload.data);
    // TODO: Update bitfield
    // Send "have" message
    // TODO: change null to bitfield
    msgHandler.sendMessage(out, MessageType.HAVE, new EmptyPayload());
    msgHandler.receiveMessage(in);
    // Send "request" message?
    // TODO: determine index based on inspecting bitfield
    msgHandler.sendMessage(out, MessageType.REQUEST, new RequestPayload(0));
  }
}

//need to know which peer sent the message

private void handleChokeReceived(Message received)
{
 //Maybe choke back?
}

private void handleUnchokeReceived(Message received)
{
  //Compare bitfield of send to ours to see what data to request
  //send a request payload back with the chosen index



  if (pieceIndex != -1)
  {
    msgHandler.sendMessage(out, MessageType.REQUEST, new RequestPayload(pieceIndex));
  }
}
}