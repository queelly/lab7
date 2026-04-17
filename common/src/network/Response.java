package network;

import models.Worker;

import java.io.Serializable;
import java.util.ArrayDeque;

public class Response implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String message;
    private final boolean success;
    private ArrayDeque<Worker> collection = null;

    public Response(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public Response(String message, ArrayDeque<Worker> collection, boolean success) {
        this(message, success);
        this.collection = collection;
    }

    public ArrayDeque<Worker> getCollection() {
        return collection;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
