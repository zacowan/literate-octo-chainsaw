package main.logging;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class DebugLogger {
  public static DebugLogger instance = null;

  String peerID;

  public DebugLogger(String peerID) {
    this.peerID = peerID;
  }

  private String getClassMethodName() {
    StackTraceElement el = Thread.currentThread().getStackTrace()[3];
    return String.format("%s.%s", el.getClassName(), el.getMethodName());
  }

  public void log(String msg, Object... args) {
    String formatted = String.format(msg, args);
    System.out.printf("[Peer %s:%s] %s\n", peerID, getClassMethodName(), formatted);
  }

  public void err(String msg, Object... args) {
    String formatted = String.format(msg, args);
    System.err.printf("[Peer %s:%s] %s\n", peerID, getClassMethodName(), formatted);
  }
}
