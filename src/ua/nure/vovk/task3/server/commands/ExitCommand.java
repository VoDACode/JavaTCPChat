package ua.nure.vovk.task3.server.commands;

import ua.nure.vovk.task3.server.CommandType;

@CommandType(name = "exit", description = "Exits from the chat")
public class ExitCommand extends BaseCommand {
    @Override
    public void execute(CommandContext context) {
        context.getClientThread().sendMessage("Bye!");
        context.getClientThread().close();

        context.getServer().removeClient(context.getClientThread());
    }
}
