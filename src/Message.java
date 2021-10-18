import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Message implements Serializable {
  public MessageType type;
  public Object payload;

  public Message(MessageType type, Object payload) {
    this.type = type;
    this.payload = payload;
  }
}
