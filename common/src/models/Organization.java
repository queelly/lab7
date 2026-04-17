package models;

import utility.Validatable;

import java.io.Serializable;

public class Organization implements Validatable, Serializable {

    private static final long serialVersionUID = 1L;

    private final Double annualTurnover; //Поле может быть null, Значение поля должно быть больше 0
    private final Integer employeesCount; //Поле может быть null, Значение поля должно быть больше 0

    public Organization(Double annualTurnover, Integer employeesCount) {
        this.annualTurnover = annualTurnover;
        this.employeesCount = employeesCount;
    }

    public Double getAnnualTurnover() {
        return annualTurnover;
    }

    public Integer getEmployeesCount() {
        return employeesCount;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "annualTurnover=" + annualTurnover +
                ";employeesCount=" + employeesCount +
                '}';
    }
}
