package com.github.thierryabalea.ticket_sales.web;

import org.rapidoid.http.fast.On;

public class WebMain {

    public static void main(String[] args) throws Exception {
        On.port(7070);

        RequestServer requestServer = new RequestServer();
        requestServer.init();

        ResponseServer responseServer = new ResponseServer();
        responseServer.init();
    }
}
