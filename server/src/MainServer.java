import commands.*;
import manager.*;
import network.Serializer;
import database.*;
import auth.UserManager;

import java.net.InetSocketAddress;

import static java.lang.System.exit;

/**
 * Главный класс сервера.
 * ИЗМЕНЕНО: Добавлена инициализация БД, убрано хранение в файле.
 */
public class MainServer {

    private static final int PORT = 11113;

    public static void main(String[] args) {

        // ДОБАВЛЕНО: Инициализация таблиц в БД
        WorkerDatabaseManager.initWorkerTable();
        UserManager.initUserTable();

        // ИЗМЕНЕНО: Загрузка коллекции из БД вместо файла
        CollectionManager collectionManager = WorkerDatabaseManager.loadWorkersFromDB();
        PrinterManager printerManager = new PrinterManager();
        Serializer serializer = new Serializer();
        LoggerManager logger = new LoggerManager(MainServer.class);

        CommandManager commandManager = new CommandManager();
        commandManager.addCommand("add",
                new AddCommand(collectionManager));
        commandManager.addCommand("add_if_max",
                new AddIfMaxCommand(collectionManager));
        commandManager.addCommand("add_if_min",
                new AddIfMinCommand(collectionManager));
        commandManager.addCommand("clear",
                new ClearCommand(collectionManager));
        commandManager.addCommand("filter_less_than_position",
                new FilterLessThanPositionCommand(collectionManager));
        commandManager.addCommand("filter_starts_with_name",
                new FilterStartsWithNameCommand(collectionManager));
        commandManager.addCommand("help",
                new HelpCommand(commandManager));
        commandManager.addCommand("info",
                new InfoCommand(collectionManager));
        commandManager.addCommand("login",
                new LoginCommand());
        commandManager.addCommand("print_field_ascending_salary",
                new PrintFieldAscendingSalaryCommand(collectionManager));
        commandManager.addCommand("remove_by_id",
                new RemoveByIdCommand(collectionManager));
        commandManager.addCommand("remove_head",
                new RemoveHeadCommand(collectionManager));
        commandManager.addCommand("show",
                new ShowCommand(collectionManager));
        ServerRuntimeManager serverRuntimeManager = new ServerRuntimeManager(new InetSocketAddress(PORT),
                commandManager,
                collectionManager,
                serializer
        );
        if (serverRuntimeManager.init()) {
            serverRuntimeManager.start();
        } else {
            logger.error("Ошибка при инициализации сервера");
            exit(1);
        }
    }
}