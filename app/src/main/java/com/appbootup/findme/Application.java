package com.appbootup.findme;

import com.squareup.otto.Bus;

public class Application extends android.app.Application {

    private static Bus sEventBus;

    public static Bus getEventBus() {
        if (sEventBus == null) {
            sEventBus = new com.squareup.otto.Bus();
        }
        return sEventBus;
    }

}