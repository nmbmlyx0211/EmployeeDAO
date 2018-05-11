package assignments;


public class DepartmentException extends Exception {

    public DepartmentException(String msg) {
        super(msg);
    }

    public DepartmentException(String msg, Throwable t) {
        super(msg, t);
    }

}

