package com.stevenschoen.imagesearcher;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.stevenschoen.imagesearcher.model.ImageResult;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageHolder> {

    public List<ImageResult> images;

    private OnItemClickListener itemClickListener;

    public ImagesAdapter(List<ImageResult> images) {
        super();
        this.images = images;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.imageresult_grid, parent, false);

        ImageHolder holder = new ImageHolder(view);
        holder.root = view;
        holder.image = (ImageView) view.findViewById(R.id.imageresult_grid_image);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, final int position) {
        final ImageResult image = images.get(position);

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(holder.root, position);
                }
            }
        });
        holder.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemLongClick(holder.root, position);
                    return true;
                }
                return false;
            }
        });

        Ion.with(holder.image.getContext()).load(image.image.thumbnailLink).withBitmap().fadeIn(true).intoImageView(holder.image);
        holder.image.setContentDescription(image.title);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {
        public View root;
        public ImageView image;

        public ImageHolder(View itemView) {
            super(itemView);
        }
    }
}
