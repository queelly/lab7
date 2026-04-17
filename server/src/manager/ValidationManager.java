package manager;

import models.Coordinates;
import models.Organization;
import utility.Validatable;
import models.Worker;

public class ValidationManager {

    private ValidationManager() {
    }

    public static boolean isValidWorker(Worker worker) {
        return worker != null &&
            worker.getName() != null &&
            !worker.getName().isEmpty() &&
            worker.getCoordinates() != null &&
            (worker.getSalary() == null || worker.getSalary() >= 0) &&
            worker.getStatus() != null &&
            worker.getOrganization() != null;
    }

    public static boolean isValidCoordinates(Coordinates coordinates) {
        return coordinates != null &&
            coordinates.getX() <= 592.f &&
            coordinates.getY() != null &&
            coordinates.getY() <= 846.;
    }

    public static boolean isValidOrganization(Organization organization) {
        return organization != null &&
            (organization.getAnnualTurnover() == null || organization.getAnnualTurnover() >= 0) &&
            (organization.getEmployeesCount() == null || organization.getEmployeesCount() >= 0);
    }

    public static boolean isValidObject(Validatable o) {
        if (o != null) {
            if (o instanceof Worker) {
                return isValidWorker((Worker) o);
            } else if (o instanceof Coordinates) {
                return isValidCoordinates((Coordinates) o);
            } else if (o instanceof Organization) {
                return isValidOrganization((Organization) o);
            }
        }
        return false;
    }
}
