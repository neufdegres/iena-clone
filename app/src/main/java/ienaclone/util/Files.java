package ienaclone.util;

import java.util.ArrayList;
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
                    ArrayList<String> lines = new ArrayList<>();
                    var tmp3 = stop.getJSONArray("lignes");
                    tmp3.forEach(el -> {
                        lines.add(el.toString());
                    });

                    res.add(new Stop(code, name, lines));
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
}
