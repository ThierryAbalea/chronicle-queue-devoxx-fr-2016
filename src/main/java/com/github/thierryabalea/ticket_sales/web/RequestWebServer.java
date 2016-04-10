package com.github.thierryabalea.ticket_sales.web;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.rapidoid.http.Req;
import org.rapidoid.http.fast.On;

public class RequestWebServer {

    public interface JsonRequestHandler {
        void onRequest(JSONObject request);
    }

    private final JsonRequestHandler jsonRequestHandler;

    public RequestWebServer(JsonRequestHandler jsonRequestHandler) {
        this.jsonRequestHandler = jsonRequestHandler;
    }

    public void init() {
        On.post("/request").plain((Req req) -> {
            doPost(req);
            return "OK";
        });
    }

    private void doPost(Req req) throws Exception {
        JSONParser parser = new JSONParser(JSONParser.MODE_RFC4627);
        String stringRequest = new String(req.body());
        JSONObject request = (JSONObject) parser.parse(stringRequest);
        jsonRequestHandler.onRequest(request);
    }
}
