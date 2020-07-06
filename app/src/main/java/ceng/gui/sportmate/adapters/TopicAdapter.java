package ceng.gui.sportmate.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ceng.gui.sportmate.R;
import ceng.gui.sportmate.TopicDetailsActivity;
import ceng.gui.sportmate.model.TopicItem;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> implements Filterable {

    Context mContext;
    List<TopicItem> mData;
    List<TopicItem> mDataFiltered;

    public TopicAdapter(Context mContext, List<TopicItem> mData) {
        this.mContext = mContext;
        this.mData = mData;
        this.mDataFiltered = mData;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout;
        layout = LayoutInflater.from(mContext).inflate(R.layout.item_topic, parent, false);
        return new TopicViewHolder(layout);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final TopicViewHolder holder, int position) {

        holder.userImage.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_transition_animation));
        holder.container.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_scale_animation));

        // bind data here
        holder.txtTitle.setText(mDataFiltered.get(position).getTitle());
        String content = mDataFiltered.get(position).getContent();
        if (content.length() > 85) {
            holder.txtContent.setText(content.substring(0, 85) + "...");
        } else {
            holder.txtContent.setText(content);
        }
        holder.txtDate.setText(mDataFiltered.get(position).getDate());
        if (mDataFiltered.get(position).getUserPhoto().equals("default")) {
            holder.userImage.setImageResource(R.drawable.ic_default_profile_image);
        } else {
            Glide.with(mContext).load(mDataFiltered.get(position).getUserPhoto()).into(holder.userImage);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, TopicDetailsActivity.class);
                intent.putExtra("title", holder.txtTitle.getText().toString());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                String Key = constraint.toString();
                if (Key.isEmpty()) {

                    mDataFiltered = mData ;

                }
                else {
                    List<TopicItem> lstFiltered = new ArrayList<>();
                    for (TopicItem row : mData) {

                        if (row.getTitle().toLowerCase().contains(Key.toLowerCase())){
                            lstFiltered.add(row);
                        }

                    }

                    mDataFiltered = lstFiltered;

                }


                FilterResults filterResults = new FilterResults();
                filterResults.values= mDataFiltered;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {


                mDataFiltered = (List<TopicItem>) results.values;
                notifyDataSetChanged();

            }
        };
    }


    class TopicViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtContent, txtDate;
        ImageView userImage;
        RelativeLayout container;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            txtTitle = itemView.findViewById(R.id.tv_title);
            txtContent = itemView.findViewById(R.id.tv_description);
            txtDate = itemView.findViewById(R.id.tv_date);
            userImage = itemView.findViewById(R.id.img_user);
        }
    }
}
