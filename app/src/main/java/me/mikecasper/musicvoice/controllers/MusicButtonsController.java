package me.mikecasper.musicvoice.controllers;

import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;

public interface MusicButtonsController {

    void onPlayerStatusObtained(UpdatePlayerStatusEvent event);

    void onSongChange(SongChangeEvent event);

    void onLostPermission(LostPermissionEvent event);
}
