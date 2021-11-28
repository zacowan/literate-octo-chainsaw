package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.*;

public class PieceStorage {
    public static PieceStorage instance = null;

    private HashMap<Integer, byte[]> downloaded;
    private String fileLocation;

    /**
     * Writes out any piece data we have to the file, in order.
     */
    private void writeCurrentPiecesToFile() {
        // Open a filewriter to fileDirectoryName
        try {
            FileWriter writer = new FileWriter(fileLocation);

            // Write any data we have into the file
            for (byte[] data : downloaded.values()) {
                if (data.length > 0) {

                    String s = new String(data);
                    writer.write(s);
                }
            }

            // Close the filewriter
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            DebugLogger.instance.err("Error writing to file, %s", e.getMessage());
        }
    }

    public synchronized byte[] getPiece(int index) {
        return downloaded.get(index);
    }

    public synchronized void setPiece(int index, byte[] data) {
        downloaded.put(index, data);
        writeCurrentPiecesToFile();
    }

    public PieceStorage(boolean hasFile) {
        String directoryName = "peer_" + PeerInfoList.instance.getThisPeer().peerID;
        this.fileLocation = directoryName + "/" + CommonConfig.fileName;

        // Initialize the file
        new File(directoryName).mkdirs();
        try {
            FileWriter writer = new FileWriter(fileLocation);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            DebugLogger.instance.err(e.getMessage());
        }

        int numPieces = (int) Math
                .ceil(Double.parseDouble(CommonConfig.fileSize) / Double.parseDouble(CommonConfig.pieceSize));
        int pieceSize = Integer.parseInt(CommonConfig.pieceSize);

        if (!hasFile) {
            this.downloaded = new HashMap<Integer, byte[]>(numPieces);

        } else {
            this.downloaded = new HashMap<Integer, byte[]>(numPieces);
            // Open file and update downloaded with file data
            try {
                InputStream in = new FileInputStream(CommonConfig.fileName);
                // 1 char = 2 bytes
                // Read pieceSize / 2 characters, pieceNum times
                for (int i = 0; i < numPieces; i++) {
                    try {
                        if (i != numPieces - 1) {
                            byte[] piece = new byte[pieceSize];

                            in.read(piece, 0, pieceSize);
                            setPiece(i, piece);
                        } else {
                            // Array size should be smaller
                            int fileSize = Integer.parseInt(CommonConfig.fileSize);
                            int lastPieceSize = fileSize % pieceSize;
                            byte[] lastPiece = new byte[lastPieceSize];
                            in.read(lastPiece, 0, lastPieceSize);
                            setPiece(i, lastPiece);
                        }

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
}
