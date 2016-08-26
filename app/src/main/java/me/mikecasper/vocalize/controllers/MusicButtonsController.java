package me.mikecasper.vocalize.controllers;

import me.mikecasper.vocalize.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.vocalize.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.vocalize.services.musicplayer.events.UpdatePlayerStatusEvent;

public interface MusicButtonsController {

    void onPlayerStatusObtained(UpdatePlayerStatusEvent event);

    void onSongChange(SongChangeEvent event);

    void onLostPermission(LostPermissionEvent event);

    void tearDown();
}
