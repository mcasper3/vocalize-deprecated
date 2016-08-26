package me.mikecasper.vocalize.controllers;

import me.mikecasper.vocalize.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.vocalize.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.vocalize.services.musicplayer.events.UpdateSongTimeEvent;

public interface MusicInfoController {

    void onSongTimeUpdated(UpdateSongTimeEvent event);

    void onSongChange(SongChangeEvent event);

    void onPlayerStatusObtained(UpdatePlayerStatusEvent event);

    void tearDown();
}
