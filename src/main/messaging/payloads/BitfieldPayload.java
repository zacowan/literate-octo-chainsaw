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

  /**
   *
   * @param currPeerBitset the current peer bitset to compare.
   * @return true if currPeerBitset is interested, false otherwise.
   */
  public boolean compare(BitSet currPeerBitset) {
    for (int i = 0; i < bitfield.size(); i++) {
      boolean currPeerHas = currPeerBitset.get(i);
      boolean comparedPeerHas = bitfield.get(i);
      if (comparedPeerHas && !currPeerHas) {
        return true;
      }
    }
    return false;
  }

}
