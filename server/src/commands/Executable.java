package commands;

import models.Worker;
import network.Response;

public interface Executable {
    Response execute(String[] args, Worker worker, String username);

    default Response execute(String[] args, Worker worker) {
        return execute(args, worker, null);
    }
}