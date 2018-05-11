package assignments;

public class EmployeeException extends Exception {

    public EmployeeException(String msg) {
        super(msg);
    }

    public EmployeeException(String msg, Throwable t) {
        super(msg, t);
    }

}

