package com.example.viniciussouza.ponto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vinicius.souza on 07/04/2017.
 */

public class PointsJSONParser {

    public List<HashMap<String,String>> parse(JSONObject jObject){
        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
                 * where each json object represent a place
                 */
        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    /** Receives a JSONObject and returns a list */

    /** Parsing the Place JSON object */
    private HashMap<String, String> getPlace(JSONObject jPlace){

        HashMap<String, String> place = new HashMap<String, String>();
        String name = "-NA-";
        String adress ="-NA-";
        String rating = "";
        String id = "";

        try {
            // Extracting Place name, if available
            if(!jPlace.isNull("adress")){
                adress = jPlace.getString("adress");
            }

            // Extracting Place Vicinity, if available
            if(!jPlace.isNull("name")){
                name = jPlace.getString("name");
            }

            if(!jPlace.isNull("rating")){
                rating = jPlace.getString("rating");
            }
            if(!jPlace.isNull("id")){
                id = jPlace.getString("id");
            }

            place.put("name", name);
            place.put("adress", adress);
            place.put("rating", rating);
            place.put("id", id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}
