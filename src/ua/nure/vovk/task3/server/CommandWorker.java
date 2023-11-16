package ua.nure.vovk.task3.server;

import ua.nure.vovk.task3.server.commands.BaseCommand;
import ua.nure.vovk.task3.server.commands.CommandContext;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

public class CommandWorker {

    private static final String packageName = "ua.nure.vovk.task3.server.commands";
    private final Logger logger = Logger.getLogger(CommandWorker.class.getName());
    private final Map<String, BaseCommand> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void load(){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        File directory = new File(Objects.requireNonNull(classLoader.getResource(path)).getFile());
        logger.info("Loading commands from " + directory.getAbsolutePath());
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".class"));
            for (File file : files) {
                String className = file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(packageName + "." + className);
                } catch (ClassNotFoundException e) {
                    logger.warning("Class " + className + " not found");
                }
                if (BaseCommand.class.isAssignableFrom(clazz)) {
                    CommandType commandType = clazz.getAnnotation(CommandType.class);
                    if (commandType != null) {
                        try {
                            commands.put(commandType.name(), (BaseCommand) clazz.getDeclaredConstructor().newInstance());
                            commandDescriptions.put(commandType.name(), commandType.description());
                        } catch (InstantiationException e) {
                            logger.warning("Class " + className + " cannot be instantiated");
                        } catch (IllegalAccessException e) {
                            logger.warning("Class " + className + " cannot be accessed");
                        } catch (InvocationTargetException e) {
                            logger.warning("Class " + className + " cannot be invoked");
                        } catch (NoSuchMethodException e) {
                            logger.warning("Class " + className + " has no default constructor");
                        }
                    }
                }
            }
        }
    }

    public boolean execute(CommandContext context) {
        String commandName = context.getCommandName();
        BaseCommand command = commands.get(commandName);
        if (command != null) {
            command.execute(context);
            return true;
        }
        return false;
    }

    public BaseCommand getCommand(String commandName) {
        return commands.get(commandName);
    }

    public Set<String> getCommandNames() {
        return commands.keySet();
    }

    public String getCommandDescription(String commandName) {
        return commandDescriptions.get(commandName);
    }

    public boolean containsCommand(String commandName) {
        return commands.containsKey(commandName);
    }
}
