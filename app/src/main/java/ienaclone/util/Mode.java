package ienaclone.util;

public enum Mode {
    TOUTES_DIRECTIONS, PAR_DIRECTION, PAR_QUAI;

    public static Mode getMode(String m) {
        // TODO : enlever "N/A" 
        switch (m.toLowerCase()) {
            case "par direction (n/a)" : return PAR_DIRECTION;
            case "par quai (n/a)" : return PAR_QUAI;
        }
        return TOUTES_DIRECTIONS;
    }
}
