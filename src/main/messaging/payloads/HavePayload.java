package main.messaging.payloads;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.Utils;

public class HavePayload extends Payload {
    public int index;

    public HavePayload(int index) {
        this.index = index;
    }

    public HavePayload(byte[] bytes) {
        this.index = Utils.bytesToInt(bytes);
    }

    public byte[] getBytes() {
        return Utils.intToBytes(index);
    }

    public int getLength() {
        return 4;
    }
}
