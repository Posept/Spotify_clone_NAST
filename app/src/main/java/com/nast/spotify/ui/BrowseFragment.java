package com.nast.spotify.ui;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nast.spotify.R;
import com.nast.spotify.manager.ListManager;
import com.nast.spotify.manager.SearchPager;
import com.nast.spotify.model.AlbumNew;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;


public class BrowseFragment extends Fragment {

    public static final String TAG = "Spotify BrowseFragment";

    private RecyclerView newAlbumsRecyclerView;
    private newAlbumAdapter mAdapter;

    private ListManager listManager;
    private SearchPager.onCompleteListener listener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listManager = ListManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse,container,false);
        newAlbumsRecyclerView = view.findViewById(R.id.new_albums_RecyclerView);
        newAlbumsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        updateView();

        return view;
    }
    private void updateView(){
        List<AlbumNew> albumNewList = listManager.getAlbumNewArrayList();

        if(albumNewList.size() == 0) {
            listener = new SearchPager.onCompleteListener() {
                @Override
                public void onComplete() {
                    Log.d(TAG,"onComplete!");
                    mAdapter.notifyDataSetChanged();
                    updateView();
                }

                @Override
                public void onError(Throwable error) {

                }
            };
            SearchPager.getInstance(getContext()).getNewRelease(listener);
        }
        if(mAdapter == null)
            mAdapter = new newAlbumAdapter(albumNewList);
        newAlbumsRecyclerView.setAdapter(mAdapter);
    }





    private class newAlbumHolder extends RecyclerView.ViewHolder {
        private ImageView album_img;
        private TextView album_title;
        private TextView album_artist;

        private newAlbumHolder(View view) {
            super(view);

            album_img = view.findViewById(R.id.new_album_image);
            album_title = view.findViewById(R.id.new_album_title);
            album_artist = view.findViewById(R.id.new_artist_name);
        }

        private void bindAlbum(AlbumNew albumNew) {
            final  String title = albumNew.getTitle();
            String img_url = albumNew.getImg_url();

            StringBuilder sb = new StringBuilder();

            List<String> artists = albumNew.getArtists();
            for(String s : artists) {
                sb.append(s+", ");
            }

            String artist = sb.toString();

            int index =artist.lastIndexOf(",");
            artist = artist.substring(0,index);

            if(artist.length()>30) {
                artist = artist.substring(0,30);
                artist += "...";
            }

            album_title.setText(title);
            album_artist.setText(artist);

            Picasso.with(getContext())
                    .load(img_url)
                    .transform(new Transformation() {
                        @Override
                        public Bitmap transform(Bitmap source) {
                            final Bitmap copy = source.copy(source.getConfig(),true);
                            source.recycle();
                            return copy;
                        }

                        @Override
                        public String key() {
                            return title;
                        }
                    })
                    .into(album_img);
        }
    }


    private class newAlbumAdapter extends RecyclerView.Adapter<newAlbumHolder> {
        private List<AlbumNew> albumNewList;

        private newAlbumAdapter(List<AlbumNew> list) {albumNewList = list;}

        @Override
        public newAlbumHolder onCreateViewHolder(ViewGroup parent,int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.new_album_view,parent,false);
            return new newAlbumHolder(view);
        }
        @Override
        public void onBindViewHolder(newAlbumHolder holder,int position) {
            holder.bindAlbum(albumNewList.get(position));
        }
        @Override
        public int getItemCount() {return albumNewList.size();}
    }
}