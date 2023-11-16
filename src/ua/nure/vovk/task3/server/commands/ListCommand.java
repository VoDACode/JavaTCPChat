package ua.nure.vovk.task3.server.commands;

import ua.nure.vovk.task3.server.ClientThread;
import ua.nure.vovk.task3.server.CommandType;

import java.util.List;

@CommandType(name = "list", description = "Prints list of users")
public class ListCommand extends BaseCommand{
    @Override
    public void execute(CommandContext context) {
        context.getClientThread().sendMessage("Users:");
        List<ClientThread> users = context.getServer().getUsers().stream().filter(user -> !user.isReadOnly()).toList();
        context.getClientThread().sendMessage(users.size() + " users online");
        StringBuilder stringBuilder = new StringBuilder();
        for (ClientThread user : users) {
            stringBuilder.append(user.getLogin()).append(", ");
        }
        context.getClientThread().sendMessage(stringBuilder.toString());
    }
}
