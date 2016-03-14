package me.mikecasper.musicvoice.api;

import me.mikecasper.musicvoice.models.SpotifyUser;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpotifyApi {

    @GET("/v1/me")
    Call<SpotifyUser> getUserInfo();

    @GET("/v1/users/{user_id}/playlists")
    void getUserPlaylists(@Path("user_id") String userId, Callback<PlaylistResponse> response);

    @GET("/v1/users/{user_id}/playlists/{playlist_id}/tracks")
    void getPlaylistTracks(@Path("user_id") String userId, @Path("playlist_id") String playlistId, @Query("offset") int offset, Callback<TrackResponse> response);
}