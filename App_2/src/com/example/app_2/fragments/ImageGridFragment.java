package com.example.app_2.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.adapters.ImageAdapter;
import com.example.app_2.models.ImageObject;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.TTS;
import com.example.app_2.utils.Utils;
import com.example.app_2.views.RecyclingImageView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ImageGridFragment extends Fragment implements AdapterView.OnItemClickListener{
    private static final String TAG = "ImageGridFragment";
    private ImageView expandedImageView;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    //private ImageFetcher mImageFetcher;
    private ImageLoader imageLoader;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    	
    public ImageGridFragment(){
    	
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageLoader = new ImageLoader();
		if(App_2.actvity!=null){
			expandedImageView = (ImageView) App_2.actvity.findViewById(R.id.expanded_image);
		}
		
		int category_id = -1;
		Bundle bundle = this.getArguments();
		if(bundle !=null)
			category_id = (int) bundle.getLong("CATEGORY_ID", -1);
		
		setHasOptionsMenu(true);
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
		if(category_id != -1){
			mAdapter = new ImageAdapter(getActivity(), category_id,imageLoader);
		}else
		{
			mAdapter = new ImageAdapter(getActivity(),imageLoader);
		}
		
	}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containter, Bundle sacedInstanceState){
    	final View v = inflater.inflate(R.layout.fragment_image_grid, containter,false);
		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
	        mGridView.setAdapter(mAdapter);
	        mGridView.setOnItemClickListener(this);
	        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
	            @Override
	            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
	                // Pause fetcher to ensure smoother scrolling when flinging
	                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
	                    //mImageFetcher.setPauseWork(true);
	                } else {
	                    //mImageFetcher.setPauseWork(false);
	                }
	            }

	            @Override
	            public void onScroll(AbsListView absListView, int firstVisibleItem,
	                    int visibleItemCount, int totalItemCount) {
	            }
	        });
	        // This listener is used to get the final width of the GridView and then calculate the
	        // number of columns and the width of each column. The width of each column is variable
	        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
	        // of each view so we get nice square thumbnails.
	        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
	                new ViewTreeObserver.OnGlobalLayoutListener() {
	                    @Override
	                    public void onGlobalLayout() {
	                        if (mAdapter.getNumColumns() == 0) {
	                            final int numColumns = (int) Math.floor(
	                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
	                            if (numColumns > 0) {
	                                final int columnWidth =
	                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
	                                mAdapter.setNumColumns(numColumns);
	                                mAdapter.setItemHeight(columnWidth);
	                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
	                            }
	                        }
	                    }
	                });

	        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
    }
    
	@SuppressLint("NewApi")
	@Override
	public void onItemClick(AdapterView<?> parent, final  View thumbView, int position, long id) {
		ImageObject imgO = mAdapter.getItemAtPosition(position);							// TODO debug info
		Toast.makeText(App_2.getAppContext(), "pos:"+position+"\n"+imgO, Toast.LENGTH_SHORT).show();
		/*
		intent = new Intent(this, ImageGridActivity.class);
		startActivity(intent);
		*/
		
		if (mCurrentAnimator != null) {
	        mCurrentAnimator.cancel();
	    }
		
		expandedImageView.bringToFront();
		//App_2.actvity.getActionBar().hide();
	    // Load the high-resolution "zoomed-in" image.
		if(expandedImageView!=null){
			 String path = Images.getImageFullScreenThumbs(position);
			 App_2.actvity.speakOut(Utils.cutExtention(Images.images.get(position).getImageName()));
			 imageLoader.loadBitmap(path, expandedImageView);
		}
		
		App_2.actvity.refreshDrawer(Images.images.get(position).getId());
				

		
	    final Rect startBounds = new Rect();
	    final Rect finalBounds = new Rect();
	    final Point globalOffset = new Point();
	    thumbView.getGlobalVisibleRect(startBounds);
	    App_2.actvity.findViewById(R.id.content_frame)
	            .getGlobalVisibleRect(finalBounds, globalOffset);
	    startBounds.offset(-globalOffset.x, -globalOffset.y);
	    finalBounds.offset(-globalOffset.x, -globalOffset.y);

	    // Adjust the start bounds to be the same aspect ratio as the final
	    // bounds using the "center crop" technique. This prevents undesirable
	    // stretching during the animation. Also calculate the start scaling
	    // factor (the end scaling factor is always 1.0).
	    float startScale;
	    if ((float) finalBounds.width() / finalBounds.height()
	            > (float) startBounds.width() / startBounds.height()) {
	        // Extend start bounds horizontally
	        startScale = (float) startBounds.height() / finalBounds.height();
	        float startWidth = startScale * finalBounds.width();
	        float deltaWidth = (startWidth - startBounds.width()) / 2;
	        startBounds.left -= deltaWidth;
	        startBounds.right += deltaWidth;
	    } else {
	        // Extend start bounds vertically
	        startScale = (float) startBounds.width() / finalBounds.width();
	        float startHeight = startScale * finalBounds.height();
	        float deltaHeight = (startHeight - startBounds.height()) / 2;
	        startBounds.top -= deltaHeight;
	        startBounds.bottom += deltaHeight;
	    }

	    // Hide the thumbnail and show the zoomed-in view. When the animation
	    // begins, it will position the zoomed-in view in the place of the
	    // thumbnail.
	    thumbView.setAlpha(0f);
	    expandedImageView.setVisibility(View.VISIBLE);

	    // Set the pivot point for SCALE_X and SCALE_Y transformations
	    // to the top-left corner of the zoomed-in view (the default
	    // is the center of the view).
	    expandedImageView.setPivotX(0f);
	    expandedImageView.setPivotY(0f);

	    // Construct and run the parallel animation of the four translation and
	    // scale properties (X, Y, SCALE_X, and SCALE_Y).
	    AnimatorSet set = new AnimatorSet();
	    set
	            .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
	                    startBounds.left, finalBounds.left))
	            .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
	                    startBounds.top, finalBounds.top))
	            .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
	            startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
	                    View.SCALE_Y, startScale, 1f));
	    set.setDuration(mShortAnimationDuration);
	    set.setInterpolator(new DecelerateInterpolator());
	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	            mCurrentAnimator = null;
	        }

	        @Override
	        public void onAnimationCancel(Animator animation) {
	            mCurrentAnimator = null;
	        }
	    });
	    set.start();
	    mCurrentAnimator = set;
	    

	    // Upon clicking the zoomed-in image, it should zoom back down
	    // to the original bounds and show the thumbnail instead of
	    // the expanded image.
	    final float startScaleFinal = startScale;
	    expandedImageView.setOnClickListener(new View.OnClickListener() {
	        @SuppressLint("NewApi")
			@Override
	        public void onClick(View view) {
	            if (mCurrentAnimator != null) {
	                mCurrentAnimator.cancel();
	            }

	            // Animate the four positioning/sizing properties in parallel,
	            // back to their original values.
	            AnimatorSet set = new AnimatorSet();
	            set.play(ObjectAnimator
	                        .ofFloat(expandedImageView, View.X, startBounds.left))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.Y,startBounds.top))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.SCALE_X, startScaleFinal))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.SCALE_Y, startScaleFinal));
	            set.setDuration(mShortAnimationDuration);
	            set.setInterpolator(new DecelerateInterpolator());
	            set.addListener(new AnimatorListenerAdapter() {
	                @Override
	                public void onAnimationEnd(Animator animation) {
	                    thumbView.setAlpha(1f);
	                    expandedImageView.setVisibility(View.GONE);
	                    mCurrentAnimator = null;
	                }

	                @Override
	                public void onAnimationCancel(Animator animation) {
	                    thumbView.setAlpha(1f);
	                    expandedImageView.setVisibility(View.GONE);
	                    mCurrentAnimator = null;
	                }
	            });
	            set.start();
	            mCurrentAnimator = set;
	            //App_2.actvity.getActionBar().show();
	        }
	    });


		
	}
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }
}
