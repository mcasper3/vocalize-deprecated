package me.mikecasper.musicvoice.controllers;

import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;

public interface MusicInfoController {

    void onSongTimeUpdated(UpdateSongTimeEvent event);
}
