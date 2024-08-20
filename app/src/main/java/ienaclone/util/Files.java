package ienaclone.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import java.net.URISyntaxException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ienaclone.prim.Requests;

import org.apache.commons.io.IOUtils;

public class Files {
    
    public static ArrayList<Stop> getAllStops() {
        ArrayList<Stop> res = new ArrayList<>();

        try {
            URL a = Files.class.getResource("all_stops.json");
            File file = new File(a.toURI());

            if (file.exists()){
                InputStream is;
                is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                
                JSONObject json = new JSONObject(jsonTxt);       
                JSONArray list = json.getJSONArray("stops");
                
                list.forEach(e -> {
                    JSONObject stop = (JSONObject)e;
                    String name = stop.getString("nom");
                    String code = stop.getString("code");
                    boolean isParis = stop.getBoolean("is_paris");
                    ArrayList<String> lines = new ArrayList<>();
                    var tmp3 = stop.getJSONArray("lignes");
                    tmp3.forEach(el -> {
                        lines.add(el.toString());
                    });

                    res.add(new Stop(code, name, isParis, lines));
                });                    
            }   
        } catch (IOException | URISyntaxException e) {
            return null;
        }

        return res;
    }

    public static ArrayList<Line> getAllLines() {
        ArrayList<Line> res = new ArrayList<>();

        try {
            URL a = Files.class.getResource("all_lines.json");
            File file = new File(a.toURI());

            if (file.exists()){
                InputStream is;
                is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                
                JSONObject json = new JSONObject(jsonTxt);       
                JSONArray list = json.getJSONArray("lines");
                
                list.forEach(e -> {
                    JSONObject line = (JSONObject)e;
                    String name = line.getString("name");
                    String code = line.getString("code");
                    String color = line.getString("color");

                    res.add(new Line(name, code, color));
                });                    
            }   
        } catch (IOException | URISyntaxException e) {
            return null;
        }

        return res;
    }

    public static ArrayList<Journey> loadTestNextJourneysValues() {
        try {
            URL a = Requests.class.getResource("chelles.json");
            File file = new File(a.toURI());

            if (file.exists()){
                InputStream is;
                is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                
                JSONObject json = new JSONObject(jsonTxt);

                return Requests.parseNextJourneys(json);
            }
        } catch (IOException | URISyntaxException | JSONException e) {
            return null;
        }

        return null;
    }

    public static HashMap<String, ArrayList<Stop>> loadTestNextStopsValues() {
        try {
            URL a = Requests.class.getResource("stops_chelles.json");
            File file = new File(a.toURI());

            if (file.exists()){
                InputStream is;
                is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                
                JSONObject json = new JSONObject(jsonTxt);

                return parseTestNextStops(json);
            }
        } catch (IOException | URISyntaxException | JSONException e) {
            return null;
        }

        return null;
    }

    private static HashMap<String, ArrayList<Stop>> parseTestNextStops(JSONObject json) {
        try {
            var res = new HashMap<String, ArrayList<Stop>>();
            JSONArray data = json.getJSONArray("data");
            for (int i=0; i<data.length(); i++) {
                var tmp = data.getJSONObject(i);

                String key = tmp.getString("destinationRef");
                var values = new ArrayList<Stop>();

                var stops = tmp.getJSONArray("stops");

                for(int y=0; y<stops.length(); y++) {
                    var ref = stops.getString(y);
                    var stop = AllStopsSingleton.getInstance()
                                .getStopByCode(ref).orElse(new Stop());
                    values.add(stop);
                }

                res.put(key, values);
            }
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
    }
}
