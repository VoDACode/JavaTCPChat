package ua.nure.vovk.task3.server.commands;

import ua.nure.vovk.task3.core.Message;
import ua.nure.vovk.task3.server.ClientThread;
import ua.nure.vovk.task3.server.Server;

public class CommandContext {
    private final Server server;
    private final ClientThread clientThread;
    private final String[] args;
    private final Message message;
    private final String commandName;

    public CommandContext(Server server, ClientThread clientThread, Message message) {
        this.server = server;
        this.clientThread = clientThread;
        this.message = message;
        String[] parts = this.message.getText().split(" ");
        commandName = parts[0].replaceFirst(Server.COMMAND_PREFIX, "");
        args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
    }

    public Server getServer() {
        return server;
    }

    public ClientThread getClientThread() {
        return clientThread;
    }

    public String[] getArgs() {
        return args;
    }

    public Message getMessage() {
        return message;
    }

    public String getCommandName() {
        return commandName;
    }
}
