package p2p;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Logger {
  // Created in peerProcess
  // Singleton?
  // Thread-safe (synchronized)
  // Method for each log type
  // Private method for writing to file
  // Method for opening/closing file
  // Ask about constantly writing to file, or writing to file at end

  public static Logger instance = null;

  private String hostID;
  private String filename;
  private FileWriter writer;

  public Logger(String hostID) {
    this.hostID = hostID;
    this.filename = "peer_log_" + hostID + ".log";
    createLogFile(filename);
  }

  public void closeLogFile() {
    try {
      writer.close();
    } catch (IOException e) {
      System.err.printf("Error closing log file, %s\n", e.getMessage());
    }
  }

  private void createLogFile(String path) {
    try {
      File file = new File(filename);
      writer = new FileWriter(file.getName());
    } catch (IOException e) {
      System.err.printf("Error creating log file, %s\n", e.getMessage());
    }
  }

  private void writeLogToFile(String log) {
    try {
      writer.write(log + ".");
    } catch (IOException e) {
      System.err.printf("Error writing to log file, %s\n", e.getMessage());
    }
  }

  private String getTimestamp() {
    String dt = new Date().toString();
    return "[" + dt + "]: ";
  }

  private String getPeerString(String id) {
    return "Peer " + id;
  }

  // Zach
  public synchronized void logTCPConnectionTo(String peerID) {
    String log = getTimestamp() + getPeerString(hostID) + " makes a connection to" + getPeerString(peerID);
    writeLogToFile(log);
  }

  // Zach
  public synchronized void logTCPConnectionFrom(String peerID) {
    String log = getTimestamp() + getPeerString(hostID) + " is connected from" + getPeerString(peerID);
    writeLogToFile(log);
  }

  // Zach
  public synchronized void logChangePreferredNeighbors(ArrayList<String> preferredNeighbors) {
    String log = getTimestamp() + getPeerString(hostID) + " has the preferred neighbors ";
    for (int i = 0; i < preferredNeighbors.size(); i++) {
      log += preferredNeighbors.get(i);
      if (i != preferredNeighbors.size() - 1) {
        log += ",";
      }
    }
    writeLogToFile(log);
  }

  // sahir
  public synchronized void logChangeOptUnchokedNeighbor(String peerID) {
    String log = getTimestamp() + getPeerString(hostID) + " has  the  optimistically  unchoked  neighbor "
        + getPeerString(peerID);
    writeLogToFile(log);
  }

  // sahir
  public synchronized void logUnchoking(String peerID) {
    String log = getTimestamp() + getPeerString(peerID) + " is unchoked by " + getPeerString(hostID);
    writeLogToFile(log);
  }

  // sahir
  public synchronized void logChocking(String peerID) {
    String log = getTimestamp() + getPeerString(peerID) + " is choked by " + getPeerString(hostID);
    writeLogToFile(log);
  }

  // Joel
  public synchronized void logHaveMessage(int peerID, int pieceIndex) {
    String log = getTimestamp() + getPeerString(hostID) + " received the 'have' message from " + peerID
        + " for the piece " + pieceIndex;
    writeLogToFile(log);
  }

  // Joel
  public synchronized void logInterestedMessage(int peerID) {
    String log = getTimestamp() + getPeerString(hostID) + " received the 'interested' message from " + peerID;
    writeLogToFile(log);
  }

  // Joel
  public synchronized void logNotInterestedMessage(int peerID) {
    String log = getTimestamp() + getPeerString(hostID) + " received the 'not interested' message from " + peerID;
    writeLogToFile(log);
  }

  // Joel
  public synchronized void logDownloadPiece(int peerID, int pieceIndex, int numPieces) {
    String log = getTimestamp() + getPeerString(hostID) + " has downloaded the piece " + pieceIndex + " from " + peerID
        + ". Now the number of pieces it has is " + numPieces;
    writeLogToFile(log);
  }

  // Sahir
  public synchronized void logCompletionDownload() {
    String lof = getTimestamp() + getPeerString(hostID) + " has downloaded the complete file";
    writeLogToFile(lof);
  }
}
