package ienaclone.prim;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import ienaclone.util.Journey;
import ienaclone.util.JourneyBuilder;

public class Requests {
    private static String HOST = "https://prim.iledefrance-mobilites.fr";
    private static String API_KEY = System.getenv("prim_api");

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
        } catch (URISyntaxException | IOException | InterruptedException e) {    
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

            bld.destinationName = (String) getJsonValue(mvj, "DestinationName#0>value:String");
            bld.destinationRef = (String) getJsonValue(mvj, "DestinationRef>value:String");
            bld.journeyRef = (String) getJsonValue(mvj, "FramedVehicleJourneyRef>DatedVehicleJourneyRef:String");
            bld.missionCode = (String) getJsonValue(mvj, "JourneyNote#0>value:String");
            bld.lineRef = (String) getJsonValue(mvj, "LineRef>value:String");
            
            var mc = mvj.getJSONObject("MonitoredCall");
            if (mc == null) continue;

            bld.aimedArrivalTime = (String) getJsonValue(mc, "AimedArrivalTime:String");
            bld.aimedDepartureTime = (String) getJsonValue(mc, "AimedDepartureTime:String");
            bld.arrivalPlatform = (String) getJsonValue(mc, "ArrivalPlatformName>value:String");
            bld.arrivalStatus = (String) getJsonValue(mc, "ArrivalStatus:String");
            bld.departureStatus = (String) getJsonValue(mc, "DepartureStatus:String");
            bld.destinationDisplay = (String) getJsonValue(mc, "DestinationDisplay#0>value:String");
            bld.expectedArrivalTime = (String) getJsonValue(mc, "ExpectedArrivalTime:String");
            bld.expectedDepartureTime = (String) getJsonValue(mc, "ExpectedDepartureTime:String");
            bld.order = (int) getJsonValue(mc, "Order:int");
            bld.stopPointName = (String) getJsonValue(mc, "StopPointName#0>value:String");
            bld.vehicleAtStop = (boolean) getJsonValue(mc, "VehicleAtStop:boolean");

            bld.numberOfTrains = ((JSONArray) getJsonValue(mvj, "TrainNumbers>TrainNumberRef:Array")).length();

            res.add(new Journey(bld));
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

    private static boolean isNumber(String s) {
        return Pattern.matches("\\d+", s);
    }


    public static class NoApiKeyException extends Exception {}

}
