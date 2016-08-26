package me.mikecasper.vocalize.api;

import me.mikecasper.vocalize.api.responses.PlaylistResponse;
import me.mikecasper.vocalize.api.responses.TrackResponse;
import me.mikecasper.vocalize.models.SpotifyUser;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpotifyApi {

    @GET("/v1/me")
    Call<SpotifyUser> getUserInfo();

    @GET("/v1/me/playlists")
    Call<PlaylistResponse> getUserPlaylists();

    @GET("/v1/users/{user_id}/playlists/{playlist_id}/tracks")
    Call<TrackResponse> getPlaylistTracks(@Path("user_id") String userId, @Path("playlist_id") String playlistId, @Query("offset") int offset);
}