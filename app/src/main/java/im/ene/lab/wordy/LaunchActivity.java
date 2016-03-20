package im.ene.lab.wordy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.ene.lab.wordy.detail.ReviewActivity;
import im.ene.lab.wordy.result.ResultItem;
import io.realm.Realm;
import io.realm.Sort;

public class LaunchActivity extends Activity {

  private static final String TAG = "LaunchActivity";

  @Bind(R.id.app_name_banner) AppCompatImageView mBanner;
  @Bind(R.id.launch_image) AppCompatImageView mLaunch;
  @Bind(R.id.action_container) View mActions;
  @Bind(R.id.smile_face) AppCompatImageView mSmile;

  boolean isAnimated = false;

  @OnClick(R.id.button_start) void start() {
    startActivity(new Intent(this, MainActivity.class));
  }

  @OnClick(R.id.button_review) void review() {
    ResultItem firstItem = mRealm.where(ResultItem.class)
        .findAllSorted(ResultItem.KEY_CREATED_AT, Sort.DESCENDING)
        .first();
    startActivity(ReviewActivity.createIntent(this, firstItem));
  }

  private Realm mRealm;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_launch);
    ButterKnife.bind(this);
    mRealm = WordyApp.realm();
  }

  @Override protected void onDestroy() {
    if (mRealm != null) {
      mRealm.close();
    }
    super.onDestroy();
  }

  @Override protected void onResume() {
    super.onResume();
    if (!isAnimated) {
      mBanner.postDelayed(new Runnable() {
        @Override public void run() {
          animateHideLogo();
        }
      }, 1000);
    }
  }

  private void animateHideLogo() {
    mLaunch.animate().alpha(0.f).setDuration(350).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        animateBanner();
      }
    }).setInterpolator(new FastOutLinearInInterpolator()).start();
  }

  private void animateBanner() {
    final int transition = -864;  // Magic number, only in Nexus 5
    mBanner.animate()
        .scaleX(1.f)
        .scaleY(1.f)
        .setDuration(550)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            isAnimated = true;
            mSmile.setVisibility(View.VISIBLE);
            AnimatedVectorDrawable vectorDrawable = (AnimatedVectorDrawable) mSmile.getDrawable();
            if (vectorDrawable != null) {
              vectorDrawable.start();
            }
          }
        })
        .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
          @Override public void onAnimationUpdate(ValueAnimator animation) {
            float value = animation.getAnimatedFraction();
            ViewCompat.setTranslationY(mBanner, transition * value);
            mActions.setAlpha(value);
            mActions.setScaleX(value);
            mActions.setScaleY(value);
          }
        })
        .setInterpolator(new AccelerateDecelerateInterpolator())
        .start();
  }
}
