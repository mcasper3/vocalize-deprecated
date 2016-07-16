package me.mikecasper.musicvoice.controllers;

import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;

public class NowPlayingMusicControls implements MusicButtonsController {

    @Subscribe
    @Override
    public void onPlayerStatusObtained(UpdatePlayerStatusEvent event) {

    }

    @Subscribe
    @Override
    public void onSongChange(SongChangeEvent event) {

    }

    @Subscribe
    @Override
    public void onLostPermission(LostPermissionEvent event) {

    }

}
