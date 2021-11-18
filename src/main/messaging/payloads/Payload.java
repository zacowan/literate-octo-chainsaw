package main.messaging.payloads;

public abstract class Payload {
    public abstract byte[] getBytes();

    public abstract int getLength();
}
