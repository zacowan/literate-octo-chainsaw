package main.messaging;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.*;
import main.Utils;

public class HandshakeMessage {
  // 18 bytes
  public static final String HEADER = "P2PFILESHARINGPROJ";
  // 10 bytes
  public static final byte[] ZEROES = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

  // Size in bytes
  private final int SIZE = 32;

  // 18 bytes
  public final String header;
  // 10 bytes
  public final byte[] zeroes;
  // 4 bytes
  public int peerID;

  public HandshakeMessage(String peerID) {
    this.peerID = Integer.parseInt(peerID);
    this.header = HEADER;
    this.zeroes = ZEROES;
  }

  public HandshakeMessage(byte[] bytes) {
    byte[] headerBytes = Arrays.copyOfRange(bytes, 0, 18);
    byte[] zeroesBytes = Arrays.copyOfRange(bytes, 18, 28);
    byte[] peerIDBytes = Arrays.copyOfRange(bytes, 28, 32);

    this.header = new String(headerBytes);
    this.zeroes = zeroesBytes;
    this.peerID = Utils.bytesToInt(peerIDBytes);
  }

  public byte[] getBytes() {
    byte[] headerBytes = header.getBytes();
    byte[] peerIDBytes = Utils.intToBytes(peerID);

    byte[] bytes = new byte[SIZE];

    // Set header in total bytes
    int index = 0;
    for (byte b : headerBytes) {
      bytes[index++] = b;
    }

    // Set zeroes in total bytes
    for (byte b : zeroes) {
      bytes[index++] = b;
    }

    // Set peerID in total bytes
    for (byte b : peerIDBytes) {
      bytes[index++] = b;
    }

    return bytes;
  }
}
