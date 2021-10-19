package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Utils {
  public static byte[] intToBytes(final int data) {
    return new byte[] { (byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff),
        (byte) ((data >> 0) & 0xff), };
  }

  public static int bytesToInt(byte[] data) {
    if (data == null || data.length != 4)
      return 0x0;
    // ----------
    return (int) ( // NOTE: type cast not necessary for int
    (0xff & data[0]) << 24 | (0xff & data[1]) << 16 | (0xff & data[2]) << 8 | (0xff & data[3]) << 0);
  }
}
