package utility;

import models.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WorkerFromArrayStringFormatter {

    public static Worker workerFromArrayString(String[] arr) {
        if (arr.length != 10) {
            return null;
        }
        Long id = null;
        String name = null;
        Coordinates coordinates = null;
        LocalDateTime creationDate;
        Double salary = null;
        Position position = null;
        Status status = null;
        Organization organization = null;
        try {
            id = Long.parseLong(arr[0]);
        } catch (NumberFormatException e) {
            id = null;
        }
        name = arr[1].trim();
        float x = 0;
        try {
            x = Float.parseFloat(arr[2]);
        } catch (NumberFormatException e) {
        }
        Double y = null;
        try {
            y = Double.parseDouble(arr[3]);
        } catch (NumberFormatException e) {
        }
        coordinates = new Coordinates(x, y);
        try {
            creationDate = LocalDateTime.parse(arr[4].trim(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            creationDate = null;
        }
        try {
            salary = Double.parseDouble(arr[5].trim());
        } catch (NumberFormatException e) {
        }
        String positionString = arr[6].trim();
        for (Position value : Position.values()) {
            if (value.toString().equalsIgnoreCase(positionString)) {
                position = value;
            }
        }
        if (positionString.isEmpty() || positionString.equalsIgnoreCase("null")) {
            position = null;
        }
        String statusString = arr[7].trim();
        for (Status value : Status.values()) {
            if (value.toString().equalsIgnoreCase(statusString)) {
                status = value;
            }
        }
        if (statusString.isEmpty() || statusString.equalsIgnoreCase("null")) {
            status = null;
        }

        String organizationString = arr[8].trim();
        Double annualTurnover = null;
        try {
            annualTurnover = Double.parseDouble(arr[8]);
        } catch (NumberFormatException e) {
        }
        Integer employeesCount = null;
        try {
            employeesCount = Integer.parseInt(arr[9]);
        } catch (NumberFormatException e) {
        }
        organization = new Organization(annualTurnover, employeesCount);
        return new Worker(id, name, coordinates, creationDate, salary, position, status, organization);
    }
}
