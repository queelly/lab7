package manager;

import models.Position;
import models.Worker;

import java.time.LocalDateTime;
import java.util.*;
        import java.util.stream.Collectors;

/**
 * Класс для управления коллекцией работников в памяти.
 * ИЗМЕНЕНО: Добавлена синхронизация через Collections.synchronizedList и метод addWithoutIdGeneration.
 */
public class CollectionManager {

    private LocalDateTime initializationDateTime = LocalDateTime.now();
    // ИЗМЕНЕНО: Использование synchronizedList для потокобезопасности согласно заданию
    private final List<Worker> collection = Collections.synchronizedList(new ArrayList<>());

    public LocalDateTime getInitializationDateTime() {
        return initializationDateTime;
    }

    /**
     * Добавление работника с генерацией ID (для обычных операций).
     */
    public boolean add(Worker worker) {
        if (worker != null && ValidationManager.isValidWorker(worker)) {
            worker.setId(IdManager.generateId());
            worker.setCreationDate(LocalDateTime.now());
            collection.add(worker);
            return true;
        }
        return false;
    }

    /**
     * ДОБАВЛЕНО: Метод для добавления работника без генерации ID (при загрузке из БД).
     * Используется при инициализации коллекции из базы данных.
     */
    public boolean addWithoutIdGeneration(Worker worker) {
        if (worker != null && ValidationManager.isValidWorker(worker)) {
            worker.setCreationDate(LocalDateTime.now());
            collection.add(worker);
            return true;
        }
        return false;
    }

    public Worker findById(Long id) {
        synchronized (collection) {
            for (Worker worker : collection) {
                if (worker.getId().equals(id)) {
                    return worker;
                }
            }
        }
        return null;
    }

    public void updateId(Long id, Worker worker) {
        Worker currentWorker = findById(id);
        if (currentWorker != null && ValidationManager.isValidWorker(worker)) {
            synchronized (collection) {
                collection.remove(currentWorker);
                worker.setId(currentWorker.getId());
                collection.add(worker);
            }
        }
    }

    public boolean removeById(Long id) {
        synchronized (collection) {
            Worker worker = findById(id);
            if (worker != null) {
                IdManager.removeFromUsedIds(worker.getId());
                return collection.remove(worker);
            }
        }
        return false;
    }

    public boolean clear() {
        initializationDateTime = LocalDateTime.now();
        synchronized (collection) {
            collection.clear();
        }
        return true;
    }

    public Worker removeHead() {
        synchronized (collection) {
            if (!collection.isEmpty()) {
                Worker worker = collection.remove(0);
                IdManager.removeFromUsedIds(worker.getId());
                return worker;
            }
        }
        return null;
    }

    public boolean addIfMax(Worker worker) {
        if (!ValidationManager.isValidWorker(worker)) {
            return false;
        }
        synchronized (collection) {
            if (collection.isEmpty()) {
                return add(worker);
            }
            Worker maxWorker = Collections.max(collection);
            return worker.compareTo(maxWorker) > 0 && add(worker);
        }
    }

    public boolean addIfMin(Worker worker) {
        if (!ValidationManager.isValidWorker(worker)) {
            return false;
        }
        synchronized (collection) {
            if (collection.isEmpty()) {
                return add(worker);
            }
            Worker minWorker = Collections.min(collection);
            return worker.compareTo(minWorker) < 0 && add(worker);
        }
    }

    public ArrayDeque<Worker> filterStartsWithName(String name) {
        synchronized (collection) {
            return collection.stream().filter(object -> object.getName()
                    .startsWith(name)).collect(Collectors.toCollection(ArrayDeque::new));
        }
    }

    public ArrayDeque<Worker> filterLessThanPosition(Position position) {
        synchronized (collection) {
            return collection.stream().filter(
                            object -> object.getName() != null && object.getPosition().compareTo(position) < 0)
                    .collect(Collectors.toCollection(ArrayDeque::new));
        }
    }

    public List<Double> getSalariesAscending() {
        synchronized (collection) {
            return collection.stream().map(Worker::getSalary).sorted().toList();
        }
    }

    public ArrayDeque<Worker> getCollection() {
        synchronized (collection) {
            return new ArrayDeque<>(collection);
        }
    }

    public String getCollectionAsString() {
        synchronized (collection) {
            if (!collection.isEmpty()) {
                return collection.stream().sorted(
                                Comparator.comparing(Worker::getCoordinates)).
                        map(Worker::toString).
                        collect(Collectors.joining("\n"));
            } else {
                return "Collection is currently empty!\n";
            }
        }
    }

    public long getCollectionSize() {
        synchronized (collection) {
            return collection.size();
        }
    }

    public String getCollectionType() {
        return collection.getClass().getTypeName();
    }
}