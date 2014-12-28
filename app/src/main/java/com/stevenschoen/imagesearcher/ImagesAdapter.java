package com.stevenschoen.imagesearcher;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.stevenschoen.imagesearcher.model.Image;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageHolder> {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_MORE_BUTTON = 2;

    private MoreImagesCallback moreImagesCallback;

    public List<Image> images;

    private OnItemClickListener itemClickListener;

    public ImagesAdapter(List<Image> images) {
        super();
        this.images = images;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResourceId = R.layout.imageresult_grid;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);

        ImageHolder holder = new ImageHolder(view);
        holder.root = view;
        holder.image = (ImageView) view.findViewById(R.id.imageresult_grid_image);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case TYPE_IMAGE:
                final Image image = images.get(position);
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
                Ion.with(holder.image.getContext()).load(image.thumbnailLink).withBitmap().fadeIn(true).intoImageView(holder.image);
                holder.image.setContentDescription(image.title);
                break;
            case TYPE_MORE_BUTTON:
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (moreImagesCallback != null) {
                            moreImagesCallback.onMoreImagesPressed();
                        }
                    }
                });
                holder.root.setOnLongClickListener(null);
                holder.image.setImageResource(R.drawable.more);
                holder.image.setContentDescription("More");
        }
    }

    @Override
    public int getItemCount() {
        if (images.isEmpty()) {
            return 0;
        } else {
            return images.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < images.size()) {
            return TYPE_IMAGE;
        } else {
            return TYPE_MORE_BUTTON;
        }
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    public void setMoreImagesCallback(MoreImagesCallback callback) {
        this.moreImagesCallback = callback;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {
        public View root;
        public ImageView image;

        public ImageHolder(View itemView) {
            super(itemView);
        }
    }

    public static interface MoreImagesCallback {
        public void onMoreImagesPressed();
    }
}
