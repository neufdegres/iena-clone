package ienaclone.gui.model;

import ienaclone.gui.util.DisplayMode;
import ienaclone.util.Stop;

public class DisplaySettings {
    private Stop selected;
    private String[] filter;
    private boolean isTest;
    private DisplayMode mode;

    public Stop getSelected() {
        return selected;
    }

    public String[] getFilter() {
        return filter;
    }

    public boolean isTest() {
        return isTest;
    }

    public DisplayMode getMode() {
        return mode;
    }
    
    public void setSelected(Stop selected) {
        this.selected = selected;
    }

    public void setFilter(String[] filter) {
        this.filter = filter;
    }

    public void setTest(boolean isTest) {
        this.isTest = isTest;
    }

    public void setMode(DisplayMode mode) {
        this.mode = mode;
    }

}
