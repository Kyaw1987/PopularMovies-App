package com.freelance.android.brooklyn.popularmoviesapp.network;

import com.freelance.android.brooklyn.popularmoviesapp.model.Movies;
import com.freelance.android.brooklyn.popularmoviesapp.model.Reviews;
import com.freelance.android.brooklyn.popularmoviesapp.model.Trailers;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Administrator on 08/11/2016.
 */
public interface MovieDatabaseService {

    @GET("3/movie/{sort_by}")
    Call<Movies> discoverMovies(@Path("sort_by") String sortBy, @Query("api_key") String apiKey);

    @GET("3/movie/{id}/videos")
    Call<Trailers> findTrailersById(@Path("id") long movieId, @Query("api_key") String apiKey);

    @GET("3/movie/{id}/reviews")
    Call<Reviews> findReviewsById(@Path("id") long movieId, @Query("api_key") String apiKey);
}
