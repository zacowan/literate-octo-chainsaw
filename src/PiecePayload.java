import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class PiecePayload implements Serializable {
  int index;
  byte[] data;

  public PiecePayload(int index, byte[] data) {
    this.data = data;
  }
}
