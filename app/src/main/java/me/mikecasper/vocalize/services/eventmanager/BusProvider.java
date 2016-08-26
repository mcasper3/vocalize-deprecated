package me.mikecasper.vocalize.services.eventmanager;

import com.squareup.otto.Bus;

public class BusProvider {

    private static Bus mBus = new Bus();

    private BusProvider() { }

    public static Bus getBus() {
        return mBus;
    }
}
