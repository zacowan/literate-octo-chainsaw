package main.messaging;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.*;
import main.messaging.payloads.Payload;

public class MessageHandler {
    ObjectOutputStream out;
    ObjectInputStream in;
    Socket socket;

    public MessageHandler(Socket socket) {
        this.socket = socket;
        try {
            // initialize inputStream and outputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            DebugLogger.instance.err("Trying to connect to unknown host.");
        } catch (ConnectException e) {
            e.printStackTrace();
            DebugLogger.instance.err("Connection failed, initialize the server first.");
        } catch (IOException e) {
            e.printStackTrace();
            DebugLogger.instance.err("IO Exception");
        }
    }

    public String receiveHandshakeServer() {
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

    public boolean receiveHandshakeClient(String peerID) {
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

    public void sendHandshake(String peerID) {
        HandshakeMessage msg = new HandshakeMessage(peerID);
        try {
            out.writeObject(msg.getBytes());
            out.flush();
        } catch (Exception e) {
            DebugLogger.instance.err(e.getMessage());
        }
    }

    public void sendMessage(MessageType type, Payload payload) {
        Message msg = new Message(type, payload);
        try {
            out.writeObject(msg.getBytes());
            out.flush();
        } catch (Exception e) {
            DebugLogger.instance.err(e.getMessage());
        }
    }

    public Message receiveMessage() {
        try {
            byte[] bytes = (byte[]) in.readObject();
            return new Message(bytes);
        } catch (IOException e) {
            DebugLogger.instance.err(e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            DebugLogger.instance.err(e.getMessage());
            e.printStackTrace();
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
