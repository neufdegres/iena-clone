package ienaclone.prim;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import ienaclone.util.Stop;
import ienaclone.util.Journey;
import ienaclone.util.JourneyBuilder;

public class JourneysDataLoaderTest {
    private static final JourneysDataLoader jdl = new JourneysDataLoader(new Stop());

    static void createJourneys() {
        var jb1 = new JourneyBuilder();
        jb1.ref = "1";
        var j1 = new Journey(jb1);
        var jb2 = new JourneyBuilder();
        jb2.ref = "2";
        var j2 = new Journey(jb2);
        var jb3 = new JourneyBuilder();
        jb3.ref = "3";
        var j3 = new Journey(jb3);
        var jb4 = new JourneyBuilder();
        jb4.ref = "4";
        var j4 = new Journey(jb4);
        var jb5 = new JourneyBuilder();
        jb5.ref = "5";
        var j5 = new Journey(jb5);
        var jb6 = new JourneyBuilder();
        jb6.ref = "6";
        var j6 = new Journey(jb6);

        var js = new ArrayList<Journey>();
        js.addAll(List.of(j1,j2,j3,j4,j5,j6));

        jdl.clearJourneys();
        jdl.updateJourneys(js);

    }

    @Test
    void testUpdateJourneys() { // journey enlevées 
        createJourneys();

        var newest = new ArrayList<Journey>();
        System.out.println(jdl.getJourneys().size());
        newest.addAll(jdl.getJourneys());
        newest.remove(0);
        newest.remove(0);

        jdl.updateJourneys(newest);

        assertEquals(newest.size(), jdl.getJourneys().size());

        assertIterableEquals(newest, jdl.getJourneys());
    }

    @Test
    void testUpdateJourneys2() { // journeys ajoutées
        createJourneys();

        var newest = new ArrayList<Journey>();
        newest.addAll(jdl.getJourneys());

        var jb7 = new JourneyBuilder();
        jb7.ref = "7";
        var j7 = new Journey(jb7);
        var jb8 = new JourneyBuilder();
        jb8.ref = "8";
        var j8 = new Journey(jb8);

        newest.addAll(List.of(j7,j8));

        jdl.updateJourneys(newest);

        assertEquals(newest.size(), jdl.getJourneys().size());

        assertIterableEquals(newest, jdl.getJourneys());
    }

    @Test
    void testUpdateJourneys3() { // journeys ajoutées + enlevées
        createJourneys();

        var newest = new ArrayList<Journey>();
        newest.addAll(jdl.getJourneys());
        newest.remove(0);
        newest.remove(0);

        var jb7 = new JourneyBuilder();
        jb7.ref = "7";
        var j7 = new Journey(jb7);
        var jb8 = new JourneyBuilder();
        jb8.ref = "8";
        var j8 = new Journey(jb8);

        newest.addAll(List.of(j7,j8));

        jdl.updateJourneys(newest);

        assertEquals(newest.size(), jdl.getJourneys().size());

        assertIterableEquals(newest, jdl.getJourneys());
    }

    @Test
    void testUpdateJourneys4() { // newest vide
        createJourneys();

        var newest = new ArrayList<Journey>();

        jdl.updateJourneys(newest);

        assertEquals(newest.size(), jdl.getJourneys().size());

        assertIterableEquals(newest, jdl.getJourneys());
    }

}
