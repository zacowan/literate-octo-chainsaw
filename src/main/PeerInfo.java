package main;

import java.util.*;

public class PeerInfo {
  public String peerID;
  public String hostname;
  public String port;
  // boolean should be a bitfield
  // Java BitSet ??
  // HOw many total pieces
  // How many pieces we have
  public BitSet bitfield;
  public boolean hasFile;

  public PeerInfo(CommonConfig config, String peerID, String hostname, String port, String hasFile) {
    this.peerID = peerID;
    this.hostname = hostname;
    this.port = port;
    // All pieces should be here if the peer starts with a file
    int hasFileInt = Integer.parseInt(hasFile);
    this.hasFile = hasFileInt == 1 ? true : false; // hasfineInt?
    // Initialize bitfield
    int numPieces = (int) Math.ceil(Double.parseDouble(config.fileSize) / Double.parseDouble(config.pieceSize));
    this.bitfield = new BitSet(numPieces);
    if (this.hasFile) {
      for (int i = 0; i < numPieces; i++) {
        bitfield.set(i, true);
      }
    }
  }

  // TODO: implement compare method
  void compare(PeerInfo peer) {

  }
}
