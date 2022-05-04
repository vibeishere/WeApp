package com.adityasri.whatsappclone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.EventListener;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


//chat loads messages but how they are shown is written here.

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> messagesList;
        String uid="";
        Context ctx;
        DatabaseReference ref;
    @SuppressLint("WrongConstant")


    public MessageAdapter(List<Messages> messagesList,String uid,Context ctx) {
        this.messagesList = messagesList;
        this.uid = uid;
        this.ctx=ctx;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout_from,parent,false);

        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        final Messages c = messagesList.get(position);
        final String userID = c.getFrom();
        final String type = c.getType();

        if(userID.equals("")||uid.equals("")){
            Toast.makeText(ctx, "You have reached the top!", Toast.LENGTH_SHORT).show();
        }
        else{
            ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        if(!userID.equals(uid)){
                            final String thumbnailImage = snapshot.child("thumbnail").getValue(String.class);
                            Picasso.get().load(thumbnailImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_dp).into(holder.profileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(thumbnailImage).placeholder(R.drawable.default_dp).into(holder.profileImage);
                                }
                            });
                            if(type.equals("text")) {
                                holder.messageTextTo.setText(c.getMessage());
                                holder.messageTextTo.setVisibility(View.VISIBLE);

                                holder.messageTextFrom.setVisibility(View.INVISIBLE);
                                holder.messageTextFrom.setText("");
                                holder.imageViewTo.setVisibility(View.INVISIBLE);
                                holder.imageViewTo.setImageDrawable(null);
                            }else{
                                holder.messageTextTo.setText("");
                                holder.messageTextTo.setVisibility(View.INVISIBLE);
                                holder.imageViewTo.setVisibility(View.VISIBLE);
                                Picasso.get().load(c.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.imageViewTo, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(c.getMessage()).into(holder.imageViewTo);
                                    }
                                });
                                holder.messageTextFrom.setVisibility(View.INVISIBLE);
                                holder.messageTextFrom.setText("");
                            }
                            holder.imageViewFrom.setVisibility(View.INVISIBLE);
                            holder.imageViewFrom.setImageDrawable(null);
                            holder.profileImage.setVisibility(View.VISIBLE);
                            holder.messageTextTo.setBackgroundResource(R.drawable.message_text_background_to);
                        }else{
                            if(type.equals("text")) {
                                holder.messageTextFrom.setText(c.getMessage());
                                holder.messageTextFrom.setVisibility(View.VISIBLE);
                                holder.messageTextTo.setVisibility(View.INVISIBLE);
                                holder.messageTextTo.setText("");
                                holder.imageViewTo.setVisibility(View.INVISIBLE);
                                holder.imageViewTo.setImageDrawable(null);
                                holder.imageViewFrom.setVisibility(View.INVISIBLE);
                                holder.imageViewFrom.setImageDrawable(null);
                            }else{
                                holder.messageTextTo.setText("");
                                holder.messageTextTo.setVisibility(View.INVISIBLE);
                                holder.imageViewFrom.setVisibility(View.VISIBLE);
                                Picasso.get().load(c.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.imageViewFrom, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(c.getMessage()).into(holder.imageViewFrom);
                                    }
                                });
                                holder.messageTextFrom.setVisibility(View.INVISIBLE);
                                holder.messageTextFrom.setText("");
                                holder.imageViewTo.setVisibility(View.INVISIBLE);
                                holder.imageViewTo.setImageDrawable(null);
                            }
                            holder.profileImage.setVisibility(View.INVISIBLE);
                            holder.messageTextFrom.setBackgroundResource(R.drawable.message_text_background_from);
                        }
                    }else{
                        Toast.makeText(ctx, "Not able to retrieve messages!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ctx, "Error in retrieving the information!", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextTo,messageTextFrom;
        public CircleImageView profileImage;
        public ImageView imageViewTo,imageViewFrom;
        public MessageViewHolder(View view) {
            super(view);
            messageTextTo = view.findViewById(R.id.chatUserTextMessageTo);
            messageTextFrom = view.findViewById(R.id.chatUserTextMessageFrom);
            profileImage = view.findViewById(R.id.chatUserdisplayPicture);
            imageViewTo = view.findViewById(R.id.imageMessageTo);
            imageViewFrom = view.findViewById(R.id.imageMessageFrom);
        }
    }
}
