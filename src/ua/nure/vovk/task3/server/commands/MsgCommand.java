package ua.nure.vovk.task3.server.commands;

import ua.nure.vovk.task3.core.Message;
import ua.nure.vovk.task3.core.MessageCode;
import ua.nure.vovk.task3.server.ClientThread;
import ua.nure.vovk.task3.server.CommandType;

@CommandType(name = "msg", description = "Send message to user")
public class MsgCommand extends BaseCommand{
    @Override
    public void execute(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length < 2) {
            context.getClientThread().sendMessage(new Message("Server", context.getClientThread().getLogin(), "Invalid arguments", MessageCode.ERROR));
            return;
        }

        String username = args[0];
        String message = String.join(" ", args).substring(username.length() + 1);

        ClientThread user = context.getServer().getUser(username);

        if (user == null) {
            context.getClientThread().sendMessage(new Message("Server", context.getClientThread().getLogin(), "User not found", MessageCode.ERROR));
            return;
        }

        Message msg = new Message(context.getClientThread().getLogin(), username, message, MessageCode.OK);
        msg.setPrivateMessage(true);
        user.sendMessage(msg);
    }
}
