package me.mikecasper.musicvoice.services;

public class EventManagerProvider {
    private static ApplicationEventManager instance = new ApplicationEventManager();

    private EventManagerProvider() { }

    public static ApplicationEventManager getInstance() {
        return instance;
    }
}
