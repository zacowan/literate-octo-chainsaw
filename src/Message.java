import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Message implements Serializable {
  public int length;
  public MessageType type;
  public byte[] payload;

  public Message(int length, MessageType type, byte[] payload) {
    this.length = length;
    this.type = type;
    this.payload = payload;
  }

  public Message(byte[] bytes) {
    byte[] lengthBytes = Arrays.copyOfRange(bytes, 0, 4);
    byte typeByte = bytes[4];

    // Set message length
    this.length = Utils.bytesToInt(lengthBytes);

    // Set message payload
    if (this.length > 5) {
      this.payload = Arrays.copyOfRange(bytes, 5, this.length);
    } else {
      this.payload = null;
    }

    // Set message type
    switch (typeByte) {
    case 0:
      this.type = MessageType.CHOKE;
      break;

    case 1:
      this.type = MessageType.UNCHOKE;
      break;

    case 2:
      this.type = MessageType.INTERESTED;
      break;

    case 3:
      this.type = MessageType.NOT_INTERESTED;
      break;

    case 4:
      this.type = MessageType.HAVE;
      break;

    case 5:
      this.type = MessageType.BITFIELD;
      break;

    case 6:
      this.type = MessageType.REQUEST;
      break;

    case 7:
      this.type = MessageType.PIECE;
      break;

    default:
      DebugLogger.instance.err("Error determining message type");
      break;
    }
  }

  public byte[] getBytes() {
    int size = length + 4 + 1;
    byte[] bytes = new byte[size];

    // Transform length into a byte array
    byte[] lengthBytes = Utils.intToBytes(length);

    // Put length into total byte array
    int index;
    for (index = 0; index < lengthBytes.length; index++) {
      bytes[index] = lengthBytes[index];
    }

    // Put message type into total byte array
    bytes[index++] = type.value;

    // Put payload into total byte array
    if (payload != null) {
      for (byte b : payload) {
        bytes[index++] = b;
      }
    }

    return bytes;
  }
}
