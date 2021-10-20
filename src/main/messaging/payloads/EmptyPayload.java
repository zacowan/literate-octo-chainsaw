package main.messaging.payloads;

public class EmptyPayload extends Payload {
  public int getLength() {
    return 0;
  }

  public byte[] getBytes() {
    return null;
  }
}
