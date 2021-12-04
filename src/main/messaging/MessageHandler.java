package main.messaging;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.*;
import main.messaging.payloads.Payload;

public class MessageHandler {
    public String receiveHandshakeServer(ObjectInputStream in) {
        try {
            byte[] bytes = (byte[]) in.readObject();
            HandshakeMessage received = new HandshakeMessage(bytes);
            if (received.header.equals(HandshakeMessage.HEADER)
                    && Arrays.equals(received.zeroes, HandshakeMessage.ZEROES)) {
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
            if (received.header.equals(HandshakeMessage.HEADER)
                    && Arrays.equals(received.zeroes, HandshakeMessage.ZEROES)
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

    public synchronized void sendMessage(ObjectOutputStream out, MessageType type, Payload payload) {
        Message msg = new Message(type, payload);
        try {
            out.writeObject(msg.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            DebugLogger.instance.err("IOException: %s", e.getMessage());
        }
    }

    public Message receiveMessage(ObjectInputStream in) {
        try {
            byte[] bytes = (byte[]) in.readObject();
            return new Message(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            DebugLogger.instance.err("IOException: %s", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            DebugLogger.instance.err("ClassNotFoundException: %s", e.getMessage());
        }
        return null;
    }

    /*
     * public void sendUnchokeMessage(ObjectOutputStream out, MessageType type,
     * Payload payload, BitSet bitfield) { Message msg = new Message(type, payload,
     * bitfield); try { out.writeObject(msg.getBytes()); out.flush(); } catch
     * (Exception e) { DebugLogger.instance.err(e.getMessage()); } }
     */
}
