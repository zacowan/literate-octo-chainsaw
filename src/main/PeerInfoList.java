package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.DebugLogger;

import java.net.Socket;

public class PeerInfoList {

    public static PeerInfoList instance = null;

    private final ArrayList<PeerInfo> peerInfoList = new ArrayList<>();
    private int thisPeerIndex;

    public synchronized boolean checkAllPeersHaveFile() {
        for (PeerInfo p : peerInfoList) {
            if (p.hasFile == false) {
                return false;
            }
        }
        return true;
    }

    public synchronized List<PeerInfo> getList() {
        return peerInfoList;
    }

    public int getNumPeersAfterThisPeer() {
        return peerInfoList.size() - thisPeerIndex - 1;
    }

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

    public int getPeerIndex(String peerID) {
        int ret = -1;
        for (int i = 0; i < peerInfoList.size(); i++) {
            PeerInfo peer = peerInfoList.get(i);
            if (peer.peerID.equals(peerID)) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    public synchronized PeerInfo getPeer(String peerID) {
        PeerInfo ret = null;
        for (PeerInfo peer : peerInfoList) {
            if (peer.peerID.equals(peerID)) {
                ret = peer;
                break;
            }
        }
        return ret;
    }

    public synchronized void setPeerBitfieldIndex(String peerID, int i) {
        int index = getPeerIndex(peerID);
        PeerInfo peer = peerInfoList.get(index);
        peer.bitfield.set(i);
        // Check if peer has entire file
        // TODO: fix this functionality
        boolean hasFile = true;
        for (int j = 0; j < peer.bitfield.size(); j++) {
            if (!peer.bitfield.get(j)) {
                hasFile = false;
                break;
            }
        }

        peer.hasFile = hasFile;
        if (hasFile) {
            DebugLogger.instance.log("Peer %s has the file.", peerID);
        }

        peerInfoList.set(index, peer);
    }

    public void printThisBitfield() {
        PeerInfo peer = peerInfoList.get(thisPeerIndex);
        for (int j = 0; j < peer.bitfield.size(); j++) {
            DebugLogger.instance.log("(%d, %s)", j, peer.bitfield.get(j));
        }
    }

    public synchronized void updatePeer(int i, PeerInfo peer) {
        peerInfoList.set(i, peer);
    }

    public synchronized void updatePeerBitfield(int i, BitSet bitfield) {
        PeerInfo peer = peerInfoList.get(i);
        peer.bitfield = bitfield;
        peerInfoList.set(i, peer);
    }

}
