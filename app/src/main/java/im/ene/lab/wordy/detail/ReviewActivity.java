package im.ene.lab.wordy.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.lab.wordy.R;
import im.ene.lab.wordy.WordyApp;
import im.ene.lab.wordy.result.ResultItem;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ReviewActivity extends AppCompatActivity {

  @Bind(R.id.results) ViewPager mResults;

  private Realm mRealm;
  private ResultsPagerAdapter mAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_review);
    ButterKnife.bind(this);

    mRealm = WordyApp.realm();
    mAdapter = new ResultsPagerAdapter(getSupportFragmentManager(), mRealm);
    mResults.setAdapter(mAdapter);
  }

  @Override public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    View decorView = getWindow().getDecorView();
    if (hasFocus) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
  }

  private static class ResultsPagerAdapter extends FragmentStatePagerAdapter {

    private final Realm mRealm;
    private final RealmResults<ResultItem> mItems;

    public ResultsPagerAdapter(FragmentManager fm, Realm mRealm) {
      super(fm);
      this.mRealm = mRealm;
      this.mItems =
          mRealm.where(ResultItem.class).findAllSorted(ResultItem.KEY_CREATED_AT, Sort.DESCENDING);
    }

    @Override public int getCount() {
      return mItems == null ? 0 : mItems.size();
    }

    @Override public Fragment getItem(int position) {
      ResultItem item = mItems.get(position);
      return DetailFragment.newInstance(item.createdAt);
    }
  }
}
