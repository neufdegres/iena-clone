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
import javafx.scene.image.Image;
import javafx.util.Pair;

import org.apache.commons.io.IOUtils;

public class Files {
    
    public static ArrayList<Stop> getAllStops() {
        ArrayList<Stop> res = new ArrayList<>();

        try {
            URL a = Files.class.getResource("all_stops.json");
            File file = new File(a.toURI());

            if (file.exists()) {
                InputStream is;
                is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                
                JSONObject json = new JSONObject(jsonTxt);       
                JSONArray list = json.getJSONArray("stops");
                
                list.forEach(e -> {
                    JSONObject stop = (JSONObject)e;
                    String name = stop.getString("nom");
                    String pointId = stop.getString("point_id");
                    String areaId = stop.getString("area_id");
                    
                    ArrayList<String> transporterIds = new ArrayList<>();
                    JSONArray tmp1 = stop.getJSONArray("transporter_ids");
                    tmp1.forEach(el -> {
                        transporterIds.add(el.toString());
                    });

                    boolean isParis = stop.getBoolean("is_paris");
                    boolean isRATP = stop.getBoolean("is_ratp");

                    ArrayList<String> lines = new ArrayList<>();
                    JSONArray tmp2 = stop.getJSONArray("lignes");
                    tmp2.forEach(el -> {
                        lines.add(el.toString());
                    });

                    res.add(new Stop(pointId, areaId, transporterIds, name, isParis, isRATP, lines));
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

                    String path = "../gui/view/icon/" + (name != null ? name : "0") + ".png";
                    Image pictogram = new Image(Files.class.getResourceAsStream(path));

                    res.add(new Line(name, code, color, pictogram));
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

    public static HashMap<String, ArrayList<Pair<Stop, Stop.STATUS>>> loadTestNextStopsValues() {
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

    private static HashMap<String, ArrayList<Pair<Stop, Stop.STATUS>>> parseTestNextStops(JSONObject json) {
        try {
            var res = new HashMap<String, ArrayList<Pair<Stop, Stop.STATUS>>>();
            JSONArray data = json.getJSONArray("data");
            for (int i=0; i<data.length(); i++) {
                var tmp = data.getJSONObject(i);

                String key = tmp.getString("destinationRef");
                var values = new ArrayList<Pair<Stop, Stop.STATUS>>();

                var stops = tmp.getJSONArray("stops");

                for(int y=0; y<stops.length(); y++) {
                    var curr = stops.getJSONObject(y);
                    var ref = curr.getString("ref");
                    var stop = AllStopsSingleton.getInstance()
                                .getStopByAreaId(ref).orElse(new Stop());
                    var status = curr.getString("status");
                    values.add(new Pair<Stop, Stop.STATUS>(stop, Stop.getStatus(status)));
                }

                res.put(key, values);
            }
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
    }

    public static String getApiKeyFromFile() {
        String res = null;

        try {
            File file = new File(System.getProperty("user.dir") + "/../api_key.txt");

            if (file.exists()){
                InputStream is;
                is = new FileInputStream(file);
                String txt = IOUtils.toString(is, "UTF-8");
                
                if (!txt.contains("api_key=")) return null;

                res = txt.split("api_key=")[1].split("\n")[0].strip();

                if (res.length() != 32) return null;
            }
        } catch (IOException e) {
            return null;
        }

        return res;
    }

}
