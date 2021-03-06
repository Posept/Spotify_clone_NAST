package com.nast.spotify.ui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nast.spotify.MainActivity;
import com.nast.spotify.R;
import com.nast.spotify.TrackDetailActivity;
import com.nast.spotify.manager.ListManager;
import com.nast.spotify.manager.PlaybackManager;
import com.nast.spotify.manager.SearchPager;
import com.nast.spotify.model.ArtistSearch;
import com.nast.spotify.model.Music;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


public class SearchResultFragment extends Fragment implements Player.NotificationCallback {
    public static final String QUERY = "QUERY";
    public static final String TAG = "Spotify SearchResult";
    public static final String DETAIL_MUSIC = "Detail Music";

    private String query;

    public static Toolbar toolbar;
    private ImageView background_album;

    private RecyclerView mRecyclerView;
    private TrackListAdapter mAdapter;

    private SearchPager mSearchPager;
    private SearchPager.CompleteListener mSearchListener;
    private SearchPager.ArtistListener mArtistListener;

    private ListManager listManager;
    private PlaybackManager playbackManager;

    private LinearLayoutManager layoutManager;

    private SpotifyPlayer mPlayer = MainActivity.mPlayer;

    public static SearchResultFragment newInstance(String query){
        Bundle args = new Bundle();
        args.putString(QUERY, query);

        SearchResultFragment searchResultFragment = new SearchResultFragment();
        searchResultFragment.setArguments(args);

        return searchResultFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchPager = SearchPager.getInstance(getContext());
        setRetainInstance(true);
        listManager = ListManager.getInstance();
        playbackManager = PlaybackManager.getInstance();

        playbackManager.setSearchResultFragmentAdded(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        query = getArguments().getString(QUERY);


        View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        playbackManager = PlaybackManager.getInstance();
        state = playbackManager.getState();

        layoutManager = new LinearLayoutManager(getActivity());

        if(state != null){
            layoutManager.onRestoreInstanceState(state);
        }

        mRecyclerView = view.findViewById(R.id.track_list_recycler_view);
        mRecyclerView.setLayoutManager(layoutManager);

        toolbar = view.findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp, null));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().detach(SearchResultFragment.this).commit();
                PlaybackManager playbackManager = PlaybackManager.getInstance();
                playbackManager.setSearchResultFragmentAdded(false);
            }
        });

        background_album = view.findViewById(R.id.background_album_field);

        if(!query.equals("empty")) {
            queryData();
        } else {
            updateView();
        }

        return view;
    }

    private void queryData(){

        mSearchListener = new SearchPager.CompleteListener() {
            @Override
            public void onComplete(List<Track> items) {

                listManager.clearList();

                for(Track track : items){
                    Music music = new Music(
                            track.id,
                            track.uri,
                            track.name,
                            track.album.name,
                            track.album.images.get(0).url,
                            track.duration_ms,
                            track.artists.get(0).name,
                            track.artists.get(0).id
                    );

                    listManager.addTrack(music);
                }

                updateView();
            }

            @Override
            public void onError(Throwable error) {
                Log.d(TAG, error.getMessage());
            }
        };

        mSearchPager.getTracksFromSearch(query, mSearchListener);
    }

    private void updateView(){

        List<Music> mList = listManager.getTrackLists();

        if(mList.size() == 0) return;

        if(mAdapter == null)
            mAdapter = new TrackListAdapter(mList);

        mRecyclerView.setAdapter(mAdapter);

        final String artistName = mList.get(0).getArtist();

        toolbar.setTitle(artistName);

        mArtistListener = new SearchPager.ArtistListener() {
            @Override
            public void onComplete(String img_url) {
                Picasso.with(getContext())
                        .load(img_url)
                        .transform(new Transformation() {
                            @Override
                            public Bitmap transform(Bitmap source) {
                                final Bitmap copy = source.copy(source.getConfig(), true);
                                source.recycle();

                                listManager.addArtist(new ArtistSearch(artistName, copy));

                                return copy;
                            }

                            @Override
                            public String key() {
                                return query;
                            }
                        })
                        .into(background_album);
            }

            @Override
            public void onError(Throwable error) {

            }
        };
        mSearchPager.getArtist(mList.get(0).getArtist_id(), mArtistListener);
    }


    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

        if(!playerEvent.name().contains("Metadata"))

        switch (playerEvent.name()) {

            case "kSpPlaybackNotifyPlay":
                break;

            case "kSpPlaybackNotifyPause":

                String title = mPlayer.getMetadata().currentTrack.name;
                String album = mPlayer.getMetadata().currentTrack.albumName;

                Music music = ListManager.getInstance().findCurrentMusic(title, album);

                if(music != null)
                    music.setPlaying(false);
                if(mAdapter != null)
                    mAdapter.notifyDataSetChanged();

                break;

            case "kSpPlaybackNotifyTrackChanged":
                break;

            case "kSpPlaybackEventAudioFlush":
                break;

            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {

    }

    private class TrackListHolder extends RecyclerView.ViewHolder
    {
        private Music music;
        private TextView title_text;
        private TextView artist_text;
        private TextView album_text;
        private ImageButton more_button;

        private TrackListHolder(final View itemView){
            super(itemView);

            title_text = itemView.findViewById(R.id.title_field);
            artist_text = itemView.findViewById(R.id.artist_field);
            album_text = itemView.findViewById(R.id.album_field);
            more_button = itemView.findViewById(R.id.more_horiz);

            more_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getContext(), TrackDetailActivity.class);

                    Bundle args = new Bundle();
                    args.putParcelable(DETAIL_MUSIC, music);

                    intent.putExtras(args);
                    startActivity(intent);
                }
            });

            title_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String prevTitle = ListManager.getInstance().getCurrent_playing_title();
                    String prevAlbum = ListManager.getInstance().getCurrent_playing_album();

                    Music prevMusic = ListManager.getInstance().findCurrentMusic(prevTitle, prevAlbum);
                    if (prevMusic != null) {
                        prevMusic.setPlaying(false);
                    }

                    // Play music

                    music.setPlaying(true);
                    RadioFragment.music = music;
                    MainActivity.mSpotifyAppRemote.getPlayerApi().play(music.getUri());
                    ListManager.getInstance().setCurrent_playing_title(music.getTitle());
                    ListManager.getInstance().setCurrent_playing_album(music.getAlbum());
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        private void bindMusic(Music m)
        {
            music = m;

            String title = music.getTitle();
            String album = music.getAlbum();

            if(title.length() > 40){
                title = title.substring(0, 40);
                title += "...";
            }

            if(album.length() > 40){
                album = album.substring(0,40);
                album += "...";
            }

            title_text.setText(title);
            artist_text.setText(music.getArtist());
            album_text.setText(album);

            if(music.isPlaying())
                title_text.setTextColor(getResources().getColor(R.color.colorAccent, null));
            else
                title_text.setTextColor(getResources().getColor(R.color.colorWhite, null));


        }
    }

    private class TrackListAdapter extends RecyclerView.Adapter<TrackListHolder>{

        private List<Music> musicList;
        private TrackListAdapter(List<Music> list){
            musicList = list;
        }

        @Override
        public TrackListHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            View view = layoutInflater.inflate(R.layout.music, parent, false);

            return new TrackListHolder(view);
        }

        @Override
        public void onBindViewHolder(TrackListHolder holder, int position) {
            Music music = musicList.get(position);

            holder.bindMusic(music);
        }

        @Override
        public int getItemCount() {
            return musicList.size();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    Parcelable state;

    @Override
    public void onPause() {
        super.onPause();

        state = layoutManager.onSaveInstanceState();

        playbackManager = PlaybackManager.getInstance();
        playbackManager.setState(state);


        Fragment fragment = getFragmentManager().findFragmentByTag("SearchFragment");
        ((SearchFragment)fragment).refresh();
    }

}