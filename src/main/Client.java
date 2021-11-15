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
        BitSet thisBitfield = PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield;
        msgHandler.sendMessage(out, MessageType.BITFIELD, new BitfieldPayload(thisBitfield));

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
    // Store bitfield in list of peers
    BitfieldPayload payload = (BitfieldPayload) received.getPayload();
    PeerInfoList.instance.updatePeerBitfield(targetInfo.listIndex, payload.bitfield);

    // Compare bitfields to check if interested or not
    boolean interested = payload.compare(PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield);
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
    // Update bitfield
    PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield.set(payload.index, true);
    // Send "have" message
    // TODO: change EmptyPayload to HavePayload with 4-byte piece index
    msgHandler.sendMessage(out, MessageType.HAVE, new EmptyPayload());
    msgHandler.receiveMessage(in);
    // Send "request" message?
    // TODO: determine index based on inspecting bitfield
    // TODO: exit thread if all pieces from target peer have been received
    msgHandler.sendMessage(out, MessageType.REQUEST, new RequestPayload(0));
  }
}
