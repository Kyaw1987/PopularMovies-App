package com.freelance.android.brooklyn.popularmoviesapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.freelance.android.brooklyn.popularmoviesapp.R;
import com.freelance.android.brooklyn.popularmoviesapp.data.MovieContract.MovieEntry;
import com.freelance.android.brooklyn.popularmoviesapp.model.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 08/10/2016.
 */
public class MovieListsAdapter extends RecyclerView.Adapter<MovieListsAdapter.ViewHolder> {

    @SuppressWarnings("unused")
    private final static String LOG_TAG = MovieListsAdapter.class.getSimpleName();

    private final ArrayList<Movie> mMovies;
    private final Callbacks mCallbacks;

    public void add(List<Movie> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    public void add(Cursor cursor) {
        mMovies.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(MovieEntry.COL_MOVIE_ID);
                String title = cursor.getString(MovieEntry.COL_MOVIE_TITLE);
                String posterPath = cursor.getString(MovieEntry.COL_MOVIE_POSTER_PATH);
                String overview = cursor.getString(MovieEntry.COL_MOVIE_OVERVIEW);
                String rating = cursor.getString(MovieEntry.COL_MOVIE_VOTE_AVERAGE);
                String releaseDate = cursor.getString(MovieEntry.COL_MOVIE_RELEASE_DATE);
                String backdropPath = cursor.getString(MovieEntry.COL_MOVIE_BACKDROP_PATH);
                Movie movie = new Movie(id, title, posterPath, overview, rating, releaseDate, backdropPath);
                mMovies.add(movie);
            } while (cursor.moveToNext());
        }
        notifyDataSetChanged();
    }

    public interface Callbacks {
        void open(Movie movie, int position);
    }

    public MovieListsAdapter(ArrayList<Movie> mMovies, Callbacks mCallbacks) {
        this.mMovies = mMovies;
        this.mCallbacks = mCallbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_content, parent, false);
        final Context context = view.getContext();

        int gridColumnNo = context.getResources().getInteger(R.integer.grid_number_cols);

        view.getLayoutParams().height = (int) (parent.getWidth() / gridColumnNo * Movie.POSTER_ASPECT_RATIO);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Movie movie = mMovies.get(position);
        final Context context = viewHolder.mView.getContext();

        viewHolder.mMovie = movie;
        viewHolder.mTitleView.setText(movie.getTitle());

        String posterUrl = movie.getPosterUrl(context);
        if (null == posterUrl) {
            viewHolder.mTitleView.setVisibility(View.VISIBLE);
        }

        Picasso.with(context).load(movie.getPosterUrl(context))
                .config(Bitmap.Config.RGB_565)
                .into(viewHolder.mThumbnailView,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                if (viewHolder.mMovie.getId() != movie.getId()) {
                                    viewHolder.cleanUp();
                                } else {
                                    viewHolder.mThumbnailView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError() {
                                viewHolder.mTitleView.setVisibility(View.VISIBLE);
                            }
                        });

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.open(movie, viewHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanUp();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @Bind(R.id.thumbnail)
        ImageView mThumbnailView;
        @Bind(R.id.title)
        TextView mTitleView;
        public Movie mMovie;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

        public void cleanUp() {
            final Context context = mView.getContext();
            Picasso.with(context).cancelRequest(mThumbnailView);
            mThumbnailView.setImageBitmap(null);
            mThumbnailView.setVisibility(View.INVISIBLE);
            mTitleView.setVisibility(View.GONE);
        }
    }
}
