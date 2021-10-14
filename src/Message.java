import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Message {
  public MessageType type;
  public Optional<ArrayList<Byte>> payload;

  public Message(MessageType type, Optional<ArrayList<Byte>> payload) {
    this.type = type;
    this.payload = payload;
  }
}
