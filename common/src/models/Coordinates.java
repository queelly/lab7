package models;

import utility.Validatable;

import java.io.Serializable;

public class Coordinates implements Validatable, Serializable, Comparable<Coordinates> {

    private static final long serialVersionUID = 1L;

    private final float x; //Максимальное значение поля: 592
    private final Double y; //Максимальное значение поля: 846, Поле не может быть null

    public Coordinates(float x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Double getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    @Override
    public int compareTo(Coordinates other) {
        if (this.x != other.getX()) {
            return this.y.compareTo(other.getY());
        }
        return Float.compare(this.x, other.getX());
    }

    @Override
    public String toString() {
        return x + ";" + y;
    }
}