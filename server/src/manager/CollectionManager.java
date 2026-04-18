package manager;

import models.Position;
import models.Worker;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CollectionManager {

    private LocalDateTime initializationDateTime = LocalDateTime.now();
    private Collection<Worker> collection = Collections.synchronizedCollection(new ArrayDeque<>());

    public LocalDateTime getInitializationDateTime() {
        return initializationDateTime;
    }

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

    public void setCollection(Collection<Worker> collection) {
        this.collection = collection;
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
                                Comparator.comparing(Worker::getId)).
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