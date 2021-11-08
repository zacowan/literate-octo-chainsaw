package main.messaging.payloads;

import java.util.*;

public class BitfieldPayload extends Payload {
  public BitSet bitfield;

  public int getLength() {
    return bitfield.toByteArray().length;
  }

  public byte[] getBytes() {
    return bitfield.toByteArray();
  }

  public BitfieldPayload(BitSet bitfield) {
    this.bitfield = bitfield;
  }

  public BitfieldPayload(byte[] bytes) {
    this.bitfield = BitSet.valueOf(bytes);
  }

}
