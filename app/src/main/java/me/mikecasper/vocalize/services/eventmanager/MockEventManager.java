package me.mikecasper.vocalize.services.eventmanager;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class MockEventManager implements IEventManager {
    private Bus mBus;

    MockEventManager() {
        mBus = new Bus(ThreadEnforcer.ANY);
    }

    @Override
    public void register(Object object) {
        mBus.register(object);
    }

    @Override
    public void unregister(Object object) {
        mBus.unregister(object);
    }

    @Override
    public void postEvent(Object object) {
        mBus.post(object);
    }
}
