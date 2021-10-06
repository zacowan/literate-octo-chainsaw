import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.Socket;

public class Test {

  public static void main(String args[]) {
    for (int i = 0; i < 5; i++) {
      new Handler(i + 1).start();
    }
  }

  public static class Handler extends Thread {
    int num;

    public void run() {
      System.out.printf("Thread %d - Hello, world!\n", num);
    }

    public Handler(int i) {
      num = i;
    }
  }
}
