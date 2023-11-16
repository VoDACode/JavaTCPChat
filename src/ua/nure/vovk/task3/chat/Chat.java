package ua.nure.vovk.task3.chat;

import ua.nure.vovk.task3.core.Message;
import ua.nure.vovk.task3.core.MessageCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Logger;

public class Chat {
    private final String host;
    private final int port;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public Chat(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);

            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            Message authorizeMessage = new Message("", "", MessageCode.READ_ONLY);
            outputStream.writeObject(authorizeMessage);

            Message response = null;
            try {
                Object object = inputStream.readObject();
                if (object instanceof Message) {
                    response = (Message) object;
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Invalid message from server");
            }

            if (socket.isConnected()) {
                if (response != null && response.getCode() == MessageCode.OK.getCode()) {
                    System.out.println("You are connected");
                    return true;
                } else {
                    System.out.println("Something went wrong while connecting to server");
                    return false;
                }
            } else {
                System.out.println("Server is not available");
                return false;
            }
        } catch (IOException e) {
            System.out.println("Server is not available");
            return false;
        }
    }

    public void work(PrintStream stream) {
        if(stream == null) {
            return;
        }

        try {
            while (socket.isConnected()) {
                Message message = getMessage();

                if (message == null) {
                    stream.println("Invalid message from server");
                    continue;
                }

                if (message.getCode() == MessageCode.OK.getCode()) {
                    stream.println(message);
                } else if (message.getCode() == MessageCode.FORBIDDEN.getCode()) {
                    stream.println("You are banned");
                    break;
                } else if (message.getCode() == MessageCode.UNAUTHORIZED.getCode()) {
                    stream.println("You are not authorized");
                    break;
                } else if (message.getCode() == MessageCode.BAD_REQUEST.getCode()) {
                    stream.println("Invalid request");
                    break;
                } else if (message.getCode() == MessageCode.EXIT.getCode()) {
                    stream.println("You are disconnected");
                    break;
                } else {
                    stream.println("Invalid message from server");
                    break;
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            stream.println("Server is not available");
        } finally {
            close();
        }
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            System.out.println("Error while closing connection");
        }
    }

    private Message getMessage() throws IOException, ClassNotFoundException {
        Object obj = inputStream.readObject();
        if (!(obj instanceof Message message)) {
            System.out.println("Invalid message type");
            return null;
        }
        return message;
    }
}
