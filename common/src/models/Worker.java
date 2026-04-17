package models;

import utility.Validatable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Worker implements Comparable<Worker>, Validatable, Serializable {

    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private final String name; //Поле не может быть null, Строка не может быть пустой
    private final Coordinates coordinates; //Поле не может быть null
    private LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private final Double salary; //Поле может быть null, Значение поля должно быть больше 0
    private final Position position; //Поле может быть null
    private final Status status; //Поле не может быть null
    private final Organization organization; //Поле не может быть null

    public Worker(
            Long id,
            String name,
            Coordinates coordinates,
            LocalDateTime creationDate,
            Double salary,
            Position position,
            Status status,
            Organization organization
    ) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.salary = salary;
        this.position = position;
        this.status = status;
        this.organization = organization;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Double getSalary() {
        return salary;
    }

    public Status getStatus() {
        return status;
    }

    public Position getPosition() {
        return position;
    }

    public Organization getOrganization() {
        return organization;
    }

    @Override
    public int compareTo(Worker other) {
        return this.id.compareTo(other.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Worker worker = (Worker) o;
        return Objects.equals(id, worker.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, salary, position, status, organization);
    }

    @Override
    public String toString() {
        return "Worker{" +
                "id: " + id +
                ", name: \"" + name + "\"" +
                ", coordinates: " + coordinates +
                ", creationDate: " + creationDate.format(DateTimeFormatter.ISO_DATE_TIME) +
                ", salary: " + salary +
                ", position: " + position +
                ", status: " + status +
                ", organization: " + organization +
                '}';
    }
}