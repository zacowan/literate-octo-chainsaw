public class PeerInfo {
  String peerID;
  String hostname;
  String port;
  // boolean should be a bitfield
  // Java BitSet ??
  // HOw many total pieces
  // How many pieces we have
  boolean hasFile;

  public PeerInfo(String peerID, String hostname, String port, String hasFile) {
    this.peerID = peerID;
    this.hostname = hostname;
    this.port = port;
    // All pieces should be here if the peer starts with a file
    this.hasFile = Boolean.parseBoolean(hasFile);
  }

  // TODO: implement compare method
}
