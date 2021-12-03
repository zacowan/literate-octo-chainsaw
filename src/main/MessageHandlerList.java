package main;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import main.logging.*;
import main.messaging.*;
import main.messaging.payloads.*;

public class MessageHandlerList {
    public static MessageHandlerList instance = null;

    private HashMap<String, MessageHandler> messageHandlers;

    public MessageHandlerList() {
        this.messageHandlers = new HashMap<>();
    }

    public synchronized void sendMessage(String target, MessageType type, Payload payload) {
        DebugLogger.instance.log("Sending message to %s", target);
        messageHandlers.get(target).sendMessage(type, payload);
        DebugLogger.instance.log("Finished sending message to %s", target);
    }

    public synchronized Message receiveMessage(String target) {
        DebugLogger.instance.log("Receiving message from %s", target);
        return messageHandlers.get(target).receiveMessage();
    }

    public synchronized void insertMessageHandler(String target, MessageHandler m) {
        messageHandlers.put(target, m);
    }

    public synchronized void sendMessagesToAll(MessageType type, Payload payload) {
        for (MessageHandler m : messageHandlers.values()) {
            m.sendMessage(type, payload);
        }
    }
}
