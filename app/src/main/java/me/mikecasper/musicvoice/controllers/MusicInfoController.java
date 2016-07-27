package me.mikecasper.musicvoice.controllers;

import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;

public interface MusicInfoController {

    void onSongTimeUpdated(UpdateSongTimeEvent event);

    void onSongChange(SongChangeEvent event);

    void onPlayerStatusObtained(UpdatePlayerStatusEvent event);

    void tearDown();
}
