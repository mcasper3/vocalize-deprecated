package me.mikecasper.musicvoice.api;

import me.mikecasper.musicvoice.api.responses.PlaylistResponse;
import me.mikecasper.musicvoice.api.responses.TrackResponse;
import me.mikecasper.musicvoice.models.SpotifyUser;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpotifyApi {

    @GET("/v1/me")
    Call<SpotifyUser> getUserInfo();

    @GET("/v1/users/{user_id}/playlists")
    Call<PlaylistResponse> getUserPlaylists(@Path("user_id") String userId);

    @GET("/v1/users/{user_id}/playlists/{playlist_id}/tracks")
    Call<TrackResponse> getPlaylistTracks(@Path("user_id") String userId, @Path("playlist_id") String playlistId, @Query("offset") int offset);
}