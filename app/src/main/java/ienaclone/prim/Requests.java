package ienaclone.prim;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import ienaclone.util.AllLinesSingleton;
import ienaclone.util.AllStopsSingleton;
import ienaclone.util.Functions;
import ienaclone.util.Journey;
import ienaclone.util.JourneyBuilder;
import ienaclone.util.TimeStatus;

public class Requests {
    private static String HOST = "https://prim.iledefrance-mobilites.fr";
    private static String API_KEY = Functions.getApiKey();

    public static HashMap<String, ArrayList<Journey>> getNextJourneys(String monitoringRef) {
        HashMap<String, ArrayList<Journey>> res = new HashMap<>();

        HttpClient client = HttpClient.newHttpClient();

        String url = HOST + "/marketplace/stop-monitoring?MonitoringRef=STIF%3AStopPoint%3AQ%3A"
                     + monitoringRef + "%3A";

        try {
            if (API_KEY == null) throw new NoApiKeyException();

            HttpRequest request = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .header("apikey", API_KEY)
                .uri(new URI(url))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            final JSONObject jsonRep = new JSONObject(response.body());

            res.put("data", parseNextJourneys(jsonRep));

        } catch (ConnectException e1) {
            res.put("error_internet", null);
        } catch (NoApiKeyException e) {
            res.put("error_apikey", null);
        } catch (Exception e) { 
            res.put("error_else", null);
        }
        
        return res;
    }

    public static ArrayList<Journey> parseNextJourneys(JSONObject json) {
        ArrayList<Journey> res = new ArrayList<>();

        var journeys = (JSONArray) getJsonValue(json, 
            "Siri>ServiceDelivery>StopMonitoringDelivery#0>MonitoredStopVisit:Array");

        for(int i=0; i<journeys.length(); i++) {
            var c = journeys.getJSONObject(i);
            JourneyBuilder bld = new JourneyBuilder();

            var mvj = c.optJSONObject("MonitoredVehicleJourney");

            if (mvj == null) continue;

            var destinationRef = (String) getJsonValue(mvj, "DestinationRef>value:String");
            bld.destination = AllStopsSingleton.getInstance()
                                .getStopByCode(destinationRef).orElse(null);

            var refRaw = (String) getJsonValue(mvj, "FramedVehicleJourneyRef>DatedVehicleJourneyRef:String");
            bld.ref = getJourneyRef(refRaw);
            bld.mission = (String) getJsonValue(mvj, "JourneyNote#0>value:String");
            
            var lineRef = (String) getJsonValue(mvj, "LineRef>value:String");
            bld.line = AllLinesSingleton.getInstance().getLineByCode(lineRef).orElse(null);

            // pour retirer les TER
            if (bld.line == null) continue;
            
            var mc = mvj.getJSONObject("MonitoredCall");
            if (mc == null) continue;

            bld.platform = (String) getJsonValue(mc, "ArrivalPlatformName>value:String");
            
            var aimedArrivalTime = (String) getJsonValue(mc, "AimedArrivalTime:String");
            bld.aimedArrivalTime = Functions.getDateTime(aimedArrivalTime).orElse(null);
            
            var aimedDepartureTime = (String) getJsonValue(mc, "AimedDepartureTime:String");
            bld.aimedDepartureTime = Functions.getDateTime(aimedDepartureTime).orElse(null);
            
            var expectedArrivalTime = (String) getJsonValue(mc, "ExpectedArrivalTime:String");
            bld.expectedArrivalTime = Functions.getDateTime(expectedArrivalTime).orElse(null);
            
            var expectedDepartureTime = (String) getJsonValue(mc, "ExpectedDepartureTime:String");
            bld.expectedDepartureTime = Functions.getDateTime(expectedDepartureTime).orElse(null);

            var statusD = (String) getJsonValue(mc, "DepartureStatus:String");
            var statusA = (String) getJsonValue(mc, "ArrivalStatus:String");

            if (statusD.equals("cancelled") || statusA.equals("cancelled"))
                bld.timeStatus = TimeStatus.CANCELLED;
            else
                bld.timeStatus = TimeStatus.ON_TIME;

            res.add(new Journey(bld));
        }

        return res;
    }

    public static HashMap<String, ArrayList<StopData>> getJourneyStopList(String ref) {
        HashMap<String, ArrayList<StopData>> res = new HashMap<>();

        HttpClient client = HttpClient.newHttpClient();

        String url = HOST + "/marketplace/v2/navitia/vehicle_journeys/"
                     + ref + "?disable_disruption=true";

        try {
            if (API_KEY == null) throw new NoApiKeyException();

            HttpRequest request = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .header("apikey", API_KEY)
                .uri(new URI(url))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            final JSONObject jsonRep = new JSONObject(response.body());

            var data = parseJourneyStopList(jsonRep);

            if (data == null) res.put("unknown_ref", null);
            else res.put("data", data);

        } catch (ConnectException e1) {
            res.put("error_internet", null);
        } catch (NoApiKeyException e) {
            res.put("error_apikey", null);
        } catch (Exception e) {    
            res.put("error_else", null);
            e.printStackTrace();
        }

        return res;
    }

    public static ArrayList<StopData> parseJourneyStopList(JSONObject json) {
        ArrayList<StopData> res = new ArrayList<>();
        
        var stops = (JSONArray) getJsonValue(json, "vehicle_journeys#0>stop_times:Array"); 

        if (stops == null) return null;

        for(int i=0; i<stops.length(); i++) {
            var stopJson = stops.getJSONObject(i);

            var stopRef = (String) getJsonValue(stopJson, "stop_point>codes#1>value:String");
            var pickupAllowed = (boolean) getJsonValue(stopJson, "pickup_allowed:boolean");;
            var dropOffAllowed = (boolean) getJsonValue(stopJson, "drop_off_allowed:boolean");;
            var skippedStop = (boolean) getJsonValue(stopJson, "skipped_stop:boolean");

            var data = new StopData(stopRef, pickupAllowed, dropOffAllowed, skippedStop);

            res.add(data);
        }

        return res;
    }

    // ex : MonitoredVehicleJourney>MonitoredCall>DestinationDisplay#0>value:String>
    // on admet que s est bien formé
    private static Object getJsonValue(JSONObject last, String s) {
        var tab = s.split(">");
        for (var key : tab) {
            // cas d'un tableau
            if (key.split("#").length == 2) {
                var set = key.split("#");
                if (isNumber(set[1])) {
                    var tmpArray = last.optJSONArray(set[0]);
                    if (tmpArray == null) break;
                    int idx = Integer.parseInt(set[1]);
                    last = tmpArray.optJSONObject(idx);
                    if (last == null) break;
                }
                // cas d'une feuille
            } else if (key.split(":").length == 2) {
                var leaf = key.split(":");
                switch (leaf[1]) {
                    case "String":
                        return last.optString(leaf[0]);
                    case "int":
                        return last.optInt(leaf[0]);              
                    case "boolean":
                        return last.optBoolean(leaf[0]);  
                    default:
                        return last.opt(leaf[0]);
                }
                // cas d'un objet json
            } else {
                last = last.optJSONObject(key);
                if (last == null) break;
            }
        }
        return null;
    }

    private static String getJourneyRef(String raw) {
        var tab = raw.split(":");
        return "vehicle_journey:IDFM:TN:SNCF:" + tab[3];
    }

    private static boolean isNumber(String s) {
        return Pattern.matches("\\d+", s);
    }

 
    public static record StopData(String stopRef, boolean pickupAllowed,
                                boolean dropOffAllowed, boolean skippedStop) {}

    public static class NoApiKeyException extends Exception {}

}
