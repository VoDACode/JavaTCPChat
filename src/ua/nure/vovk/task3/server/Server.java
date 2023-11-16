package ua.nure.vovk.task3.server;

import ua.nure.vovk.task3.core.Message;
import ua.nure.vovk.task3.core.MessageCode;
import ua.nure.vovk.task3.server.commands.CommandContext;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Server {
    public static final String SERVER_NAME = "Server";
    public static final String COMMAND_PREFIX = "/";
    public static final String CLIENT_RECORDS_FILE = "users.properties";
    private final Logger logger = Logger.getLogger(SERVER_NAME);
    private ServerSocket serverSocket;
    private final CommandWorker commandWorker;
    private final List<ClientThread> clientThreads;
    private final List<ClientRecord> clientRecordsStorage = new ArrayList<>();
    private boolean isStopped = false;
    private final int port;

    public Server(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be in range 0..65535");
        }
        this.port = port;
        this.clientThreads = new ArrayList<>();
        this.commandWorker = new CommandWorker();

        logger.info("Loading commands");
        this.commandWorker.load();
        logger.info("Loaded " + this.commandWorker.getCommandNames().size() + " commands");

        this.loadClientRecords();
    }

    public void start() throws IOException {
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            throw new IllegalStateException("Server is already started");
        }

        this.serverSocket = new ServerSocket(port);
        logger.info("Server started on port " + port);
        while (!this.serverSocket.isClosed() && !isStopped) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientThread clientThread = new ClientThread(clientSocket, this);
                clientThread.start();
                clientThreads.add(clientThread);
            } catch (IOException e) {
                if(!this.serverSocket.isClosed()) {
                    logger.severe("Error while accepting client");
                }
                break;
            }
        }

        for (ClientThread clientThread : clientThreads) {
            clientThread.interrupt();
        }
    }

    public void stop() {
        if (this.serverSocket.isClosed()) {
            throw new IllegalStateException("Server is already stopped");
        }
        Message message = new Message(SERVER_NAME, "*", "Server is stopped", MessageCode.BYE);
        for (ClientThread clientThread : clientThreads) {
            clientThread.sendMessage(message);
        }

        try {
            this.serverSocket.close();
            logger.info("Server stopped");
        } catch (IOException e) {
            logger.severe("Error while closing server socket");
        }
        this.isStopped = true;
    }

    public boolean executeCommand(Message message, ClientThread sender) {
        CommandContext context = new CommandContext(this, sender, message);
        return commandWorker.execute(context);
    }

    public void broadcast(String message) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        Message messageObject = new Message(SERVER_NAME, "*", message, MessageCode.OK);
        broadcast(messageObject, null);
    }

    public void broadcast(Message message, ClientThread sender) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        for (ClientThread clientThread : clientThreads) {
            if (clientThread == sender || !clientThread.isReadOnly()) {
                continue;
            }
            clientThread.sendMessage(message);
        }
    }

    public void removeClient(ClientThread clientThread) {
        clientThreads.remove(clientThread);
    }

    public AuthStatus isLoginAndPasswordCorrect(String login, String password) {
        for (ClientRecord clientRecord : clientRecordsStorage) {
            if (clientRecord.getLogin().equals(login)) {
                if (clientRecord.isPasswordCorrect(password)) {
                    return AuthStatus.SUCCESS_AUTH;
                } else {
                    return AuthStatus.INCORRECT_PASSWORD;
                }
            }
        }
        return AuthStatus.LOGIN_NOT_FOUND;
    }

    public void addClientRecord(String login, String password) {
        clientRecordsStorage.add(new ClientRecord(login, password));
        saveClientRecords();
        logger.info("Added new client record: " + login);
    }

    public CommandWorker getCommandWorker() {
        return commandWorker;
    }

    private void loadClientRecords() {
        logger.info("Loading client records");
        try {
            File file = new File(CLIENT_RECORDS_FILE);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length != 2) {
                    continue;
                }
                clientRecordsStorage.add(new ClientRecord(parts[0], parts[1]));
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            logger.severe("Error while loading client records");
        }
        logger.info("Loaded " + clientRecordsStorage.size() + " client records");
    }

    private void saveClientRecords() {
        try {
            File file = new File(CLIENT_RECORDS_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (ClientRecord clientRecord : clientRecordsStorage) {
                bufferedWriter.write(clientRecord.toString());
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            fileWriter.close();

            logger.info("Saved " + clientRecordsStorage.size() + " client records");
        } catch (IOException e) {
            logger.severe("Error while saving client records");
        }
    }

    public List<ClientThread> getUsers() {
        return clientThreads;
    }

    public ClientThread getUser(String login) {
        for (ClientThread clientThread : clientThreads) {
            if (!clientThread.isReadOnly() && clientThread.getLogin().equals(login)) {
                return clientThread;
            }
        }
        return null;
    }

    public boolean isStoppedServer() {
        return isStopped;
    }
}
