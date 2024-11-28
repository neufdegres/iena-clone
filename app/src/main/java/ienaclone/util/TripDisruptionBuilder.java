package ienaclone.util;

import ienaclone.util.TripDisruption.CATEGORY;
import ienaclone.util.TripDisruption.CAUSE;
import ienaclone.util.TripDisruption.EFFECT;
import ienaclone.util.TripDisruption.STATUS;
import ienaclone.util.TripDisruption.TAG;

public class TripDisruptionBuilder {
    public String id;
    public String lastUpdated;
    public STATUS status;
    public TAG tag;
    public CAUSE cause;
    public CATEGORY category;
    public EFFECT effect;
    public String color;
    public int priority;
    public String title, message;

    public TripDisruptionBuilder() {
        this.id = "";
        this.lastUpdated = "";
        this.status = STATUS.ACTIVE;
        this.tag = TAG.NONE;
        this.cause = CAUSE.NONE;
        this.category = CATEGORY.NONE;
        this.effect = EFFECT.OTHER_EFFECT;
        this.color = "#000000";
        this.priority = 0;
        this.title = "";
        this.message = "";
    }

}
