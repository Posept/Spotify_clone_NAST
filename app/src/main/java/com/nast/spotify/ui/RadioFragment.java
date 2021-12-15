package com.nast.spotify.ui;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.nast.spotify.MainActivity;
import com.nast.spotify.R;
import com.nast.spotify.model.Music;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.types.PlayerState;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.concurrent.TimeUnit;


public class RadioFragment extends Fragment {

    ImageButton btnPlay, btnNext, btnPrevious, btnRandom, btnLoop, btnPause;
    TextView txtTimeStart, txtTimeEnd, txtSingerName, txtSongName;
    SeekBar songBar;
    ObjectAnimator animator;
    ImageView img_song;
    PlayerState playerState;
    public static Music music;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (MainActivity.mSpotifyAppRemote.getPlayerApi().getPlayerState()!=null){
            CallResult<PlayerState> playerStateCall = MainActivity.mSpotifyAppRemote.getPlayerApi().getPlayerState();
            Result<PlayerState> playerStateResult = playerStateCall.await(200, TimeUnit.MICROSECONDS);
            if (playerStateResult.isSuccessful()) {
                playerState = playerStateResult.getData();
            } else {
                Throwable error = playerStateResult.getError();
            }
        }
        View view = inflater.inflate(R.layout.fragment_radio,container,false);

        btnPause=view.findViewById(R.id.pause);
        btnPlay=view.findViewById(R.id.play);
        btnNext=view.findViewById(R.id.next);
        btnLoop=view.findViewById(R.id.loop);
        btnPrevious=view.findViewById(R.id.previous);
        btnRandom=view.findViewById(R.id.random);

        if(music!=null){
            img_song = view.findViewById(R.id.img_song);
            txtSingerName = view.findViewById(R.id.singerName);
            txtSongName = view.findViewById(R.id.songName);
            txtTimeEnd = view.findViewById(R.id.timeEnd);
            txtTimeStart = view.findViewById(R.id.timeStart);

            txtSingerName.setText(music.getArtist());
            txtSongName.setText(music.getTitle());
            txtTimeStart.setText("0");
            txtTimeEnd.setText("0");
            Picasso.with(getContext())
                    .load(music.getAlbum_image())
                    .transform(new Transformation() {
                        @Override
                        public Bitmap transform(Bitmap source) {
                            final Bitmap copy = source.copy(source.getConfig(), true);
                            source.recycle();
                            return copy;
                        }

                        @Override
                        public String key() {
                            return music.getTitle();
                        }
                    })
                    .into(img_song);
        }

        if(playerState!=null){
            if (playerState.isPaused) {
                btnPause.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.VISIBLE);
            } else {
                btnPlay.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.INVISIBLE);
            }
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mSpotifyAppRemote.getPlayerApi().resume();
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mSpotifyAppRemote.getPlayerApi().pause();
                btnPause.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.VISIBLE);
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mSpotifyAppRemote.getPlayerApi().skipNext();
            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mSpotifyAppRemote.getPlayerApi().skipPrevious();
            }
        });
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    MainActivity.mSpotifyAppRemote.getPlayerApi().toggleShuffle();

            }
        });
        btnLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mSpotifyAppRemote.getPlayerApi().toggleRepeat();
            }

        });

        songBar = view.findViewById(R.id.seekBar);
        songBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.mSpotifyAppRemote.getPlayerApi().seekTo(songBar.getProgress());
            }
        });

        return view;
    }
}