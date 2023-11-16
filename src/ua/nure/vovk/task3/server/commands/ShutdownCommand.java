package ua.nure.vovk.task3.server.commands;

import ua.nure.vovk.task3.server.ClientThread;
import ua.nure.vovk.task3.server.CommandType;

@CommandType(name = "shutdown", description = "Shutdown server (only in localhost)")
public class ShutdownCommand extends BaseCommand{
    @Override
    public void execute(CommandContext context) {
        ClientThread client = context.getClientThread();
        if (client.isLocalhost()) {
            context.getServer().stop();
        } else {
            client.sendMessage("You can't shutdown server from remote host");
        }
    }
}
