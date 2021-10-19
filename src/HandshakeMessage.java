import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class HandshakeMessage implements Serializable {
  public static final String HEADER = "P2PFILESHARINGPROJ";
  public static final byte[] ZEROES = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

  public final String header;
  public final byte[] zeroes;
  public int peerID;

  public HandshakeMessage(String peerID) {
    this.peerID = Integer.parseInt(peerID);
    this.header = HEADER;
    this.zeroes = ZEROES;
  }
}
