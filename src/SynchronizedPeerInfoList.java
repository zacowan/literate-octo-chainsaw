import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.Socket;

public class SynchronizedPeerInfoList {

  public static SynchronizedPeerInfoList instance = null;

  private final ArrayList<PeerInfo> peerInfoList = new ArrayList<>();
  private int thisPeerIndex;

  public void setThisPeerIndex(int i) {
    thisPeerIndex = i;
  }

  public int getThisPeerIndex() {
    return thisPeerIndex;
  }

  public synchronized PeerInfo getThisPeer() {
    return peerInfoList.get(thisPeerIndex);
  }

  public synchronized void addPeer(PeerInfo peer) {
    peerInfoList.add(peer);
  }

  public synchronized int getSize() {
    return peerInfoList.size();
  }

  public synchronized PeerInfo getPeer(int i) {
    return peerInfoList.get(i);
  }

  public synchronized void updatePeer(int i, PeerInfo peer) {
    peerInfoList.set(i, peer);
  }

}
