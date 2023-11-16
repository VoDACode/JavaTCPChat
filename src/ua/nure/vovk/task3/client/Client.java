package ua.nure.vovk.task3.client;

import ua.nure.vovk.task3.core.Message;
import ua.nure.vovk.task3.core.MessageCode;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client {
    private final int port;
    private final String host;
    private String login;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Scanner scanner;

    private Thread inputThread;
    private Thread outputThread;

    public Client(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            Message authorizeMessage = new Message("", "", MessageCode.READ_WRITE);
            outputStream.writeObject(authorizeMessage);
        } catch (Exception e) {
            System.out.println("Failed to connect to server");
        }
    }

    public boolean authorize(String login, String password) {
        if (socket == null) {
            System.out.println("You must connect to server first");
            return false;
        }
        if (socket.isClosed()) {
            System.out.println("You must connect to server first");
            return false;
        }
        try {
            outputStream.writeObject(new Message(login, password, MessageCode.AUTHORIZE));
            Object object = inputStream.readObject();
            if (!(object instanceof Message response)) {
                System.out.println("Unknown response from server");
                return false;
            }
            if (response.getCode() == MessageCode.OK.getCode()) {
                this.login = login;
                return true;
            }
        } catch (Exception e) {
            System.out.println("Failed to authorize");
        }
        return false;
    }

    public void work() {
        if (login == null) {
            throw new IllegalStateException("You must authorize first");
        }
        inputThread = new Thread(this::inputHandler);
        inputThread.start();
        outputThread = new Thread(this::outputHandler);
        outputThread.start();
    }

    public boolean isConnectionAlive() {
        return socket != null && !socket.isClosed();
    }

    public void sendMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        Message messageObject = new Message(login, message, MessageCode.OK);
        sendMessage(messageObject);
    }

    public void sendMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        try {
            outputStream.writeObject(message);
        } catch (Exception e) {
            System.out.println("Failed to send message");
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Failed to close socket");
        }
        try {
            outputStream.close();
        } catch (Exception e) {
            System.out.println("Failed to close output stream");
        }
        try {
            inputStream.close();
        } catch (Exception e) {
            System.out.println("Failed to close input stream");
        }
        outputThread.interrupt();
        inputThread.interrupt();
    }

    private void inputHandler() {
        scanner = new Scanner(System.in);
        while (isConnectionAlive() && !Thread.currentThread().isInterrupted()) {
            try {
                String message = scanner.nextLine();
                if(message == null || message.isEmpty()) {
                    continue;
                }
                if (message.equals("exit")) {
                    close();
                    break;
                }
                if(!isConnectionAlive()) {
                    break;
                }
                sendMessage(message);
            } catch (Exception e) {
                break;
            }
        }
        close();
    }

    private void outputHandler() {
        while (isConnectionAlive() && !Thread.currentThread().isInterrupted()) {
            try {
                Object object = inputStream.readObject();
                if (!(object instanceof Message message)) {
                    System.out.println("Unknown message from server");
                    continue;
                }
                System.out.println(message);
                if(message.getCode() == MessageCode.BYE.getCode()) {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        close();
        System.out.println("Connection closed");
        System.out.println("Press enter to exit");
    }
}
