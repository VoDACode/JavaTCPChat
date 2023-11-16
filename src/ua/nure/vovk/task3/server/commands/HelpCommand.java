package ua.nure.vovk.task3.server.commands;

import ua.nure.vovk.task3.server.ClientThread;
import ua.nure.vovk.task3.server.CommandType;
import ua.nure.vovk.task3.server.CommandWorker;
import ua.nure.vovk.task3.server.Server;

@CommandType(name = "help", description = "Prints help")
public class HelpCommand extends BaseCommand{
    @Override
    public void execute(CommandContext context) {
        Server server = context.getServer();
        CommandWorker commandWorker = server.getCommandWorker();
        ClientThread clientThread = context.getClientThread();
        clientThread.sendMessage("Available commands:");
        for (String commandName : commandWorker.getCommandNames()) {
            clientThread.sendMessage(Server.COMMAND_PREFIX + commandName + " - " + commandWorker.getCommandDescription(commandName));
        }
    }
}
