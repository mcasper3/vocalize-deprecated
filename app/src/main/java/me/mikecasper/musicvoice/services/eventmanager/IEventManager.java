package me.mikecasper.musicvoice.services.eventmanager;

public interface IEventManager {
    void register(Object object);
    void unregister(Object object);
    void postEvent(Object object);
}
