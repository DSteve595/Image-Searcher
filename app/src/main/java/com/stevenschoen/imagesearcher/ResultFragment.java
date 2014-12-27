package com.stevenschoen.imagesearcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.stevenschoen.imagesearcher.model.ImageResult;

public class ResultFragment extends Fragment {

    public static final String EXTRA_IMAGE_RESULT = "imageResult";
    public static final String EXTRA_THUMBNAIL_BITMAP = "thumbnailBitmap";

    private ImageResult result;

    private Bitmap thumbnailBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thumbnailBitmap = getArguments().getParcelable(EXTRA_THUMBNAIL_BITMAP);
        result = getArguments().getParcelable(EXTRA_IMAGE_RESULT);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_THUMBNAIL_BITMAP)) {
                thumbnailBitmap = savedInstanceState.getParcelable(EXTRA_THUMBNAIL_BITMAP);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.imageresult_full, container, false);

        final ImageView thumbnailImage = (ImageView) view.findViewById(R.id.imageresult_full_thumbnail);
        if (thumbnailBitmap != null) {
            thumbnailImage.setImageBitmap(thumbnailBitmap);
        } else {
            Ion.with(getActivity()).load(result.image.thumbnailLink).intoImageView(thumbnailImage)
                    .withBitmapInfo().setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                @Override
                public void onCompleted(Exception e, ImageViewBitmapInfo result) {
                    thumbnailBitmap = result.getBitmapInfo().bitmap;
                }
            });
        }

        final ImageView image = (ImageView) view.findViewById(R.id.imageresult_full_image);
        image.setAlpha(0f);
        Ion.with(getActivity()).load(result.link).intoImageView(image)
                .withBitmapInfo().setCallback(new FutureCallback<ImageViewBitmapInfo>() {
            @Override
            public void onCompleted(Exception e, ImageViewBitmapInfo result) {
                image.animate().alpha(1f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbnailImage.setVisibility(View.GONE);
                    }
                });
            }
        });

        return view;
    }

    public ImageResult getImageResult() {
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (thumbnailBitmap != null) {
            outState.putParcelable(EXTRA_THUMBNAIL_BITMAP, thumbnailBitmap);
        }
    }
}
