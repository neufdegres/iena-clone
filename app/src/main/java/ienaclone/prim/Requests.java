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
import ienaclone.util.TripDisruption;
import ienaclone.util.TripDisruptionBuilder;
import ienaclone.util.StopDisruption.TYPE;
import ienaclone.util.Functions;
import ienaclone.util.Journey;
import ienaclone.util.JourneyBuilder;
import ienaclone.util.StopDisruption;
import ienaclone.util.TimeStatus;
import ienaclone.util.TrainLength;
import ienaclone.util.TripDisruption.CATEGORY;
import ienaclone.util.TripDisruption.CAUSE;
import ienaclone.util.TripDisruption.EFFECT;
import ienaclone.util.TripDisruption.STATUS;
import ienaclone.util.TripDisruption.TAG;

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

            AllStopsSingleton allStops = AllStopsSingleton.getInstance();

            bld.destination = allStops.getStopByAreaId(destinationRef)
                                .orElse(allStops
                                .getStopByPointId(destinationRef)
                                .orElse(allStops
                                .getStopByTransporterId(destinationRef)
                                .orElse(null)));

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

            var lengthArray = mvj.optJSONArray("VehicleFeatureRef");
            if (lengthArray.isEmpty())
                bld.trainLength = TrainLength.UNKNOWN;
            else {
                var length = lengthArray.getString(0);
                if (length.equals("shortTrain"))
                    bld.trainLength = TrainLength.SHORT;
                else
                    bld.trainLength = TrainLength.LONG;
            }
                
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

    public static HashMap<String, ArrayList<TripDisruption>> getTripDisruptions(String ref) {
        HashMap<String, ArrayList<TripDisruption>> res = new HashMap<>();

        HttpClient client = HttpClient.newHttpClient();

        String url = HOST + "/marketplace/v2/navitia/vehicle_journeys/" + ref;

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

            var data = parseTripDisruptions(jsonRep);

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

    public static ArrayList<TripDisruption> parseTripDisruptions(JSONObject json) {
        ArrayList<TripDisruption> res = new ArrayList<>();
        
        var disr = (JSONArray) getJsonValue(json, "disruptions:Array"); 

        if (disr == null) return null;

        for(int i=0; i<disr.length(); i++) {
            var db = new TripDisruptionBuilder();

            var disrJson = disr.getJSONObject(i);

            db.id = (String) getJsonValue(disrJson, "id:String");

            db.lastUpdated = (String) getJsonValue(disrJson, "updated_at:String");
            
            var statusStr = (String) getJsonValue(disrJson, "status:String");
            TripDisruption.STATUS status;
            switch (statusStr) {
                case "past": status = STATUS.PAST; break;
                case "active": status = STATUS.ACTIVE; break;
                default: status = STATUS.FUTURE; break;
            }
            db.status = status;

            /* var tagStr = (String) getJsonValue(disrJson, "tags#0>String");
            TripDisruption.TAG tag;
            if (tagStr != null)  {
                switch (statusStr) {
                    case "Actualité": tag = TAG.ACTUALITE; break;
                    default: tag = TAG.NONE; break;
                }
            } else {
                tag = TAG.NONE;
            }
            db.tag = tag; */ // TODO

            db.tag = TAG.NONE;

            var causeStr = (String) getJsonValue(disrJson, "cause:String");
            TripDisruption.CAUSE cause;
            if (causeStr != null)  {
                switch (causeStr) {
                    case "perturbation": cause = CAUSE.PERTUBATION; break;
                    case "travaux": cause = CAUSE.TRAVAUX; break;
                    default: cause = CAUSE.NONE; break;
                }
            } else {
                cause = CAUSE.NONE;
            }
            db.cause = cause;

            var categoryStr = (String) getJsonValue(disrJson, "category:String");
            CATEGORY category;
            if (categoryStr != null)  {
                switch (categoryStr) {
                    case "Incidents": category = CATEGORY.INCIDENTS; break;
                    default: category = CATEGORY.NONE; break;
                }
            } else {
                category = CATEGORY.NONE;
            }
            db.category = category;

            var severityJson = disrJson.optJSONObject("severity");

            var effectStr = (String) getJsonValue(severityJson, "effect:String");
            EFFECT effect;
            if (effectStr != null) {
                switch (effectStr) {
                    case "SIGNIFICANT_DELAYS": effect = EFFECT.SIGNIFICANT_DELAYS; break;
                    case "ADDITIONAL_SERVICE": effect = EFFECT.ADDITIONAL_SERVICE; break;
                    case "NO_SERVICE": effect = EFFECT.NO_SERVICE; break;
                    case "REDUCED_SERVICE": effect = EFFECT.REDUCED_SERVICE; break;
                    default: effect = EFFECT.OTHER_EFFECT; break;
                }
            } else {
                effect = EFFECT.OTHER_EFFECT;
            }
            db.effect = effect;

            db.color = (String) getJsonValue(severityJson, "color:String");

            db.priority = (int) getJsonValue(severityJson, "priority:int");

            var messagesJson = (JSONArray) getJsonValue(disrJson, "messages:Array");

            if (messagesJson == null) {
                // pour le moment on skip
                continue;
            }

            String title = "", message = "";

            for (int j=0; j<messagesJson.length(); j++) {
                if (!title.isEmpty() && !message.isEmpty()) break;

                var tmpMessJson = messagesJson.getJSONObject(j);

                var type = (String)getJsonValue(tmpMessJson, "channel>name:String");
                var text = (String)getJsonValue(tmpMessJson, "text:String");

                if (type.equals("moteur")) {
                    message = text;
                } else {
                    title = text;
                }
            }
            db.title = title;
            db.message = message;

            var data = new TripDisruption(db);

            res.add(data);
        }

        return res;
    }

    public static HashMap<String, ArrayList<StopDisruption>> getStopDisruptions(String ref) {
        HashMap<String, ArrayList<StopDisruption>> res = new HashMap<>();

        HttpClient client = HttpClient.newHttpClient();
        
        String url = HOST + "/marketplace/general-message?StopPointRef=STIF%3AStopPoint%3AQ%3A"
                     + ref + "%3A";

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

            var data = parseStopDisruptions(jsonRep);

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

    public static ArrayList<StopDisruption> parseStopDisruptions(JSONObject json) {
        ArrayList<StopDisruption> res = new ArrayList<>();

        // System.out.println(json.toString(4));

        var disruptions = (JSONArray) getJsonValue(json, 
            "Siri>ServiceDelivery>GeneralMessageDelivery#0>InfoMessage:Array");

        for(int i=0; i<disruptions.length(); i++) {
            var d = disruptions.getJSONObject(i);
            
            var id = (String) getJsonValue(d, "ItemIdentifier:String");
            
            var type_raw = (String) getJsonValue(d, "InfoChannelRef>value:String");
            TYPE type;
            switch (type_raw) {
                case "Information":
                    type = TYPE.INFORMATION;          
                    break;
                case "Perturbation":
                    type = TYPE.PERTURBATION;          
                    break;
                default:
                    type = TYPE.COMMERCIAL;
                    break;
            }

            var message = (String) getJsonValue(d, "Content>Message#0>MessageText>value:String");

            var tmp = new StopDisruption(id, type, message);
            res.add(tmp);
        }

        return res;
    }


    // ex : MonitoredVehicleJourney>MonitoredCall>DestinationDisplay#0>value:String>
    // on admet que s est bien formé     "VehicleFeatureRef#0>String"
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
