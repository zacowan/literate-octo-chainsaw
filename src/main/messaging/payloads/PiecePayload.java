package main.messaging.payloads;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.Utils;

public class PiecePayload extends Payload {
  public int index;
  public byte[] data;

  public PiecePayload(int index, byte[] data) {
    this.index = index;
    this.data = data;
  }

  public PiecePayload(byte[] bytes) {
    byte[] indexBytes = Arrays.copyOfRange(bytes, 0, 4);
    this.index = Utils.bytesToInt(indexBytes);

    this.data = Arrays.copyOfRange(bytes, 4, bytes.length);
  }

  public byte[] getBytes() {
    byte[] indexBytes = Utils.intToBytes(index);
    byte[] bytes = new byte[data.length + 4];

    // Put index into total bytes
    int index;
    for (index = 0; index < indexBytes.length; index++) {
      bytes[index] = indexBytes[index];
    }

    // Put data into total bytes
    for (byte b : data) {
      bytes[index++] = b;
    }

    return bytes;
  }

  public int getLength() {
    return 4 + data.length;
  }
}
