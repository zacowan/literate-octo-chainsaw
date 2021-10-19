import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class RequestPayload {
  int index;

  public RequestPayload(int index) {
    this.index = index;
  }

  public RequestPayload(byte[] bytes) {
    this.index = Utils.bytesToInt(bytes);
  }

  public byte[] getBytes() {
    return Utils.intToBytes(index);
  }

  public int getLength() {
    return 4;
  }
}
