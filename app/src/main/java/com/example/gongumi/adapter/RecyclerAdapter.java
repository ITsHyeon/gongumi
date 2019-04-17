package com.example.gongumi.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gongumi.R;
import com.example.gongumi.fragment.HomePostFragment;
import com.example.gongumi.model.Home;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    Context context;
    List<Home> items;
    int item_layout;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("thumnail/");
    StorageReference pathRef = storageRef;

    public RecyclerAdapter(Context context, List<Home> items, int item_layout) {
        this.context = context;
        this.items = items;
        this.item_layout = item_layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Home item = items.get(position);

        storageRef = pathRef.child(item.getTime() + "/thumbnail1.jpg");
        Log.i("thubnail", String.valueOf(storageRef));

        Glide.with(context).load(storageRef).apply(new RequestOptions().error(R.drawable.profile_photo)).into(holder.thumbnail);

        holder.product.setText(item.getProduct());
        holder.price.setText(item.getPrice());
        holder.progressBar.setMax(item.getProgress());
        holder.progressBar.setProgress(item.getPeople());
        holder.people.setText(String.valueOf(item.getPeople()) + "명이 참여했습니다.");
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.frame_home,
                        HomePostFragment.newInstance(item.getProduct(),item.getPrice(),item.getUrl(), item.getProgress(),item.getPeople(), item.getContent(), item.getTime())).commit();
                fragmentTransaction.addToBackStack(null);
            }
        });
    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView product;
        TextView price;
        ProgressBar progressBar;
        TextView people;
        CardView cardview;


        public ViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            product = (TextView) itemView.findViewById(R.id.product);
            price = (TextView) itemView.findViewById(R.id.price);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            people = (TextView) itemView.findViewById(R.id.people);
            cardview = (CardView) itemView.findViewById(R.id.cardview);

        }
    }
}
