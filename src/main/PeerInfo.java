package main;

import java.util.*;

public class PeerInfo {
  public String peerID;
  public String hostname;
  public String port;
  public BitSet bitfield;
  public int listIndex;
  public boolean hasFile;

  public PeerInfo(CommonConfig config, String peerID, String hostname, String port, String hasFile, int listIndex) {
    this.peerID = peerID;
    this.hostname = hostname;
    this.port = port;
    this.listIndex = listIndex;
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
    } else {
      for (int i = 0; i < numPieces; i++) {
        bitfield.set(i, false);
      }
    }
  }
}
