package commands;

import auth.UserManager;
import database.WorkerDatabaseManager;
import models.Worker;
import network.Response;

public interface Executable {

    Response execute(String[] args, Worker worker, String username,
                             WorkerDatabaseManager workerDatabaseManager, UserManager userManager);




}