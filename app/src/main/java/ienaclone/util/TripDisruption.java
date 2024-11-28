package ienaclone.util;

public class TripDisruption {
    // TODO : supprimer ce qui est inutile
    private final String id;
    private final String lastUpdated;
    private final STATUS status;
    private final TAG tag;
    private final CAUSE cause;
    private final CATEGORY category;
    private final EFFECT effect;
    private final String color;
    private final int priority;
    private final String title, message;

    public enum STATUS {PAST, ACTIVE, FUTURE}
    public enum TAG {ACTUALITE, NONE}
    public enum CAUSE {TRAVAUX, PERTUBATION, NONE}
    public enum CATEGORY {INCIDENTS, NONE}
    public enum EFFECT {SIGNIFICANT_DELAYS, ADDITIONAL_SERVICE, NO_SERVICE, REDUCED_SERVICE, OTHER_EFFECT}

    public TripDisruption(TripDisruptionBuilder db) {
        this.id = db.id;
        this.lastUpdated = db.lastUpdated;
        this.status = db.status;
        this.tag = db.tag;
        this.cause = db.cause;
        this.category = db.category;
        this.effect = db.effect;
        this.color = db.color;
        this.priority = db.priority;
        this.title = db.title;
        this.message = db.message;
    }

    public String getId() {
        return id;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public STATUS getStatus() {
        return status;
    }

    public TAG getTag() {
        return tag;
    }

    public CAUSE getCause() {
        return cause;
    }

    public CATEGORY getCategory() {
        return category;
    }

    public EFFECT getEffect() {
        return effect;
    }

    public String getColor() {
        return color;
    }

    public int getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TripDisruption)) return false;
        return this.id.equals(((TripDisruption)obj).id);
    }

    @Override
    public String toString() {
        return "id: " + id + "\n"
                + "status: " + status + "\n"
                + "tag: " + tag + "\n"
                + "cause: " + cause + "\n"
                + "category: " + category + "\n"
                + "effect: " + effect + "\n"
                + "color: " + color + "\n"
                + "priority: " + priority + "\n"
                + "title: " + title + "\n"
                + "message: " + message;
    }

}
