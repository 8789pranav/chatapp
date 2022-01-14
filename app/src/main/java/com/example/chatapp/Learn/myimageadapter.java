package com.example.chatapp.Learn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;


import java.util.ArrayList;

public class myimageadapter extends RecyclerView.Adapter<MyViewHolder> {
    Context mcontext;
    ArrayList<image_help> image_help;
    public myimageadapter(Context mcontext,ArrayList<image_help> image_help){
        this.mcontext=mcontext;
        this.image_help=image_help;

    }




    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mcontext).inflate(R.layout.mode_imagehelp,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
       image_help image_help1= image_help.get(position);
        holder.textView.setText(image_help1.getName());
        Glide.with(mcontext)
                .load(image_help1.getUri())

                .fitCenter()
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return image_help.size();
    }


}

class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView imageView;
    TextView textView;

    public MyViewHolder(@NonNull View itemView) {

        super(itemView);

        textView=itemView.findViewById(R.id.text_imagehelp);
        imageView=itemView.findViewById(R.id.image_view_help);
    }
}
