package network;
import manager.LoggerManager;

import java.io.*;

public class Serializer {
    private final LoggerManager logger = new LoggerManager(Serializer.class);

    public byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error when serializing data");
            return null;
        }
    }

    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object obj = objectInputStream.readObject();
            if (!clazz.isInstance(obj)) {
                logger.error("Error: an object of the " + obj.getClass() +
                        " type was received. Expected: " + clazz);
                return null;
            }
            return clazz.cast(obj);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Deserialization error");
            return null;
        }
    }
}
