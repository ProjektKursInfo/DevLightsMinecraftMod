package de.devlights.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.devlights.api.dtos.BlinkColorDto;
import de.devlights.api.dtos.BrightnessDto;
import de.devlights.api.dtos.UpdateColorDto;
import de.devlights.utils.ArraySerialiazer;
import de.devlights.utils.Color;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Api {
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(String[].class, new ArraySerialiazer<>()).create();
    private static final OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HeaderInterceptor()).build();

    enum RequestMethod{
        GET,
        POST,
        PATCH
    }

    public static Future<Boolean> setSingleColor(Color color) {
            return executeRequest("color", new UpdateColorDto("plain", color), RequestMethod.PATCH);
    }

    public static Future<Boolean> setRunnerColor(Color color, Integer timeout) {
        return executeRequest("color", new UpdateColorDto("runner", color, timeout), RequestMethod.PATCH);
    }

    public static Future<Boolean> startParty(){
        return  executeRequest("color", new UpdateColorDto("rainbow", 500), RequestMethod.PATCH);
    }

    public static Future<Boolean> blink(Color color, int time) {
        return executeRequest("blink",new BlinkColorDto(color, time), RequestMethod.POST);
    }

    public static Future<Boolean> setBrightness(int brightness) {
        return executeRequest("brightness",new BrightnessDto(brightness), RequestMethod.PATCH);
    }

    public static Future<Boolean> turnOff() {
        return executeRequest("off",null, RequestMethod.PATCH);
    }

    public static Future<Boolean> turnOn() {
        return executeRequest("on",null, RequestMethod.PATCH);
    }

    public static RequestBody generateBody(Object data){
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        return RequestBody.create(gson.toJson(data),JSON);
    }

    public static Request generateRequest(String url, Object data, RequestMethod method){
        String baseUrl = "http://devlight.local/tags/minecraft/";
        switch (method){
            case GET:
                return new Request.Builder().url(baseUrl + url).get().build();
            default:
            case POST:
                return new Request.Builder().url(baseUrl + url).post(generateBody(data)).build();
            case PATCH:
                return new Request.Builder().url(baseUrl + url).patch(generateBody(data)).build();
        }
    }

    public static Future<Boolean> executeRequest(String url, Object data, RequestMethod method) {
        return executor.submit(() -> {
            try {
                Response response = client.newCall(generateRequest(url, data, method)).execute();
                response.close();
                return response.isSuccessful();
            } catch (IOException exception) {
                return false;
            }
        });
    }
}
