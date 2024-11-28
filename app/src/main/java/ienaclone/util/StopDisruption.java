package ienaclone.util;

public class StopDisruption {
    private final String id;
    private final TYPE type;
    private final String message;

    public enum TYPE {INFORMATION, PERTURBATION, COMMERCIAL}

    public StopDisruption(String id, TYPE type, String message) {
        this.id = id;
        this.type = type;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public TYPE getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "id: " + id + "\n"
                + "type: " + type + "\n"
                + "message: " + message;
    }

}
