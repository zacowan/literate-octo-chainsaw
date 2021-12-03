package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import main.logging.*;

public class PieceStorage {
    public static PieceStorage instance = null;

    private HashMap<Integer, byte[]> downloaded;
    private String fileLocation;
    private Path filePath;

    /**
     * Writes out any piece data we have to the file, in order.
     */
    private void writeCurrentPiecesToFile() {
        for (byte[] bytes : downloaded.values()) {
            try {
                Files.write(filePath, bytes, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
                DebugLogger.instance.err("Error writing piece to file.");
            }

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
        this.filePath = Paths.get(this.fileLocation);

        // Initialize the file
        new File(directoryName).mkdirs();
        try {
            FileWriter writer = new FileWriter(fileLocation);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            DebugLogger.instance.err("Error initializing file.");
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
                        DebugLogger.instance.log("Writing out piece #%d", i);
                        if (i != numPieces - 1) {
                            byte[] piece = new byte[pieceSize];
                            in.read(piece, 0, pieceSize);
                            downloaded.put(i, piece);
                        } else {
                            // Array size should be smaller
                            int fileSize = Integer.parseInt(CommonConfig.fileSize);
                            int lastPieceSize = fileSize % pieceSize;
                            byte[] lastPiece = new byte[lastPieceSize];
                            in.read(lastPiece, 0, lastPieceSize);
                            downloaded.put(i, lastPiece);
                        }

                    } catch (IOException e) {
                        DebugLogger.instance.err("Error reading piece %d from file, %s", i, e.getMessage());
                    }
                }
                in.close();
                writeCurrentPiecesToFile();
            } catch (FileNotFoundException e) {
                DebugLogger.instance.err("Error reading file, %s", e.getMessage());
            } catch (IOException e) {
                DebugLogger.instance.err("Error closing file, %s", e.getMessage());
            }
        }
    }
}
