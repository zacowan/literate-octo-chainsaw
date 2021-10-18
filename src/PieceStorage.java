import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class PieceStorage {
  public static PieceStorage instance = null;

  private HashMap<Integer, byte[]> downloaded;

  public synchronized byte[] getPiece(int index) {
    return downloaded.get(index);
  }

  public synchronized void setPiece(int index, byte[] data) {
    downloaded.put(index, data);
  }

  public PieceStorage(int numPieces) {
    this.downloaded = new HashMap<Integer, byte[]>(numPieces);
  }

  public PieceStorage(int numPieces, int pieceSize, String filePath) {
    this.downloaded = new HashMap<Integer, byte[]>(numPieces);
    // Open file and update downloaded with file data
    try {
      InputStream in = new FileInputStream(filePath);
      // 1 char = 2 bytes
      // Read pieceSize / 2 characters, pieceNum times
      for (int i = 0; i < numPieces; i++) {
        try {
          byte[] piece = new byte[pieceSize];
          in.read(piece, 0, pieceSize);
          downloaded.put(i, piece);
        } catch (IOException e) {
          DebugLogger.instance.err("Error reading piece %d from file, %s", i, e.getMessage());
        }
      }
      in.close();
    } catch (FileNotFoundException e) {
      DebugLogger.instance.err("Error reading file, %s", e.getMessage());
    } catch (IOException e) {
      DebugLogger.instance.err("Error closing file, %s", e.getMessage());
    }
  }
}
