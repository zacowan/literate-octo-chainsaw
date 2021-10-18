import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class PieceStorage {
  public static PieceStorage instance = null;

  public ArrayList<byte[]> downloaded;

  public PieceStorage() {
    this.downloaded = new ArrayList<>();
  }

  public PieceStorage(String filePath) {
    this.downloaded = new ArrayList<>();
    // Open file and update downloaded with file data
    try {
      InputStream in = new FileInputStream(filePath);
      // 1 char = 2 bytes
      // Read pieceSize / 2 characters, pieceNum times
      int length = Integer.parseInt(CommonConfig.instance.pieceSize);
      int numPieces = Integer.parseInt(CommonConfig.instance.fileSize)
          / Integer.parseInt(CommonConfig.instance.pieceSize) + 1;
      for (int i = 0; i < numPieces; i++) {
        try {
          byte[] piece = new byte[length];
          in.read(piece, 0, length);
          downloaded.add(piece);
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
