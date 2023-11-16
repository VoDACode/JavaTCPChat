package ua.nure.vovk.task3.server;

import ua.nure.vovk.task3.core.Message;
import ua.nure.vovk.task3.core.MessageCode;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientThread extends Thread {
    private Logger logger;
    private final Socket clientSocket;
    private final Server server;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private boolean readOnly = false;

    private String login = "Chat";

    public ClientThread(Socket clientSocket, Server server) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        logger = Logger.getLogger("User[" + this.clientSocket.getInetAddress().getHostAddress() + "]");
    }

    @Override
    public void run() {
        if (authorize() != AuthStatus.SUCCESS_AUTH) {
            sendMessage(new Message(Server.SERVER_NAME, "You are not authorized", MessageCode.UNAUTHORIZED));
            return;
        }
        sendMessage(new Message(Server.SERVER_NAME, "You are connected", MessageCode.OK));

        if (readOnly) {
            logger.info("Read only connection established");
        } else {
            handleInputStream();
        }
    }

    private void handleInputStream() {
        try {
            sendMessage("Welcome to the chat, " + login);
            server.broadcast("User " + login + " has joined the chat");
            logger = Logger.getLogger("User[" + login + "]");
            logger.info("User " + login + " connected");

            while (clientSocket.isConnected() && !this.isInterrupted()) {
                boolean isCommand = false;
                Message message = getMessage();
                if (message == null) {
                    continue;
                }
                message.setFrom(login);

                if (message.getText().startsWith(Server.COMMAND_PREFIX)) {
                    isCommand = server.executeCommand(message, this);
                }

                if (clientSocket.isClosed()) {
                    break;
                }

                if (!isCommand) {
                    server.broadcast(message, this);
                }
            }
        } catch (ClassNotFoundException e) {
            logger.severe("Class not found");
        } catch (IOException e) {
            logger.severe("IO error");
        } finally {
            this.close();
        }
    }

    public void sendMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        Message messageObject = new Message(Server.SERVER_NAME, message, MessageCode.OK);
        sendMessage(messageObject);
    }

    public void sendMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        try {
            outputStream.writeObject(message);
        } catch (IOException e) {
            logger.severe("IO error while sending message");
        }
    }

    public Message getMessage() throws IOException, ClassNotFoundException {
        Object obj = inputStream.readObject();
        if (!(obj instanceof Message message)) {
            sendMessage("Invalid message type");
            return null;
        }
        message.setDateToNow();
        return message;
    }

    private AuthStatus authorize() {
        int attempts = 3;
        while (attempts > 0) {
            try {
                Message message = getMessage();

                readOnly = message.getCode() == MessageCode.READ_ONLY.getCode();
                if (readOnly) {
                    return AuthStatus.SUCCESS_AUTH;
                }

                message = getMessage();

                if (message.getCode() == MessageCode.AUTHORIZE.getCode()) {
                    setLogin(message.getFrom());
                    String password = message.getText();
                    AuthStatus status = server.isLoginAndPasswordCorrect(getLogin(), password);

                    if (status == AuthStatus.SUCCESS_AUTH) {
                        return AuthStatus.SUCCESS_AUTH;
                    } else if (status == AuthStatus.INCORRECT_PASSWORD) {
                        sendMessage("Incorrect login or password");
                        attempts--;
                        continue;
                    } else if (status == AuthStatus.LOGIN_NOT_FOUND) {
                        server.addClientRecord(getLogin(), password);
                        return AuthStatus.SUCCESS_AUTH;
                    }
                }
                sendMessage("You are not authorized");
                attempts--;
            } catch (IOException | ClassNotFoundException e) {
                logger.severe("IO error while authorizing");
                return AuthStatus.INCORRECT_PASSWORD;
            }
        }
        return AuthStatus.INCORRECT_PASSWORD;
    }

    @Override
    public void interrupt() {
        this.close();
        super.interrupt();
    }

    public void close() {
        try {
            if (!clientSocket.isClosed() && !readOnly) {
                server.broadcast("User " + login + " has left the chat");
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.severe("IO error while closing socket");
        }
        if(!server.isStoppedServer()){
            server.removeClient(this);
        }
        logger.info("User disconnected");
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isLocalhost() {
        return clientSocket.getInetAddress().isLoopbackAddress();
    }

}
