package im.ene.lab.wordy.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.lab.wordy.R;
import im.ene.lab.wordy.WordyApp;
import im.ene.lab.wordy.result.ResultItem;
import im.ene.lab.wordy.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ReviewActivity extends AppCompatActivity implements DetailFragment.Callback {

  @Bind(R.id.results) ViewPager mResults;

  public static final String EXTRAS_INIT_ITEM_ID = "extra_detail_init_item_id";

  private Long mInitItemId;

  public static Intent createIntent(Context context, ResultItem item) {
    Intent intent = new Intent(context, ReviewActivity.class);
    intent.putExtra(EXTRAS_INIT_ITEM_ID, item.createdAt);
    return intent;
  }

  private Realm mRealm;
  private RealmResults<ResultItem> mItems;
  private ResultsPagerAdapter mAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_review);
    ButterKnife.bind(this);

    mRealm = WordyApp.realm();
    mItems =
        mRealm.where(ResultItem.class).findAllSorted(ResultItem.KEY_CREATED_AT, Sort.DESCENDING);
    mAdapter = new ResultsPagerAdapter(getSupportFragmentManager(), mItems);
    mResults.setAdapter(mAdapter);

    if (getIntent() != null) {
      mInitItemId = getIntent().getLongExtra(EXTRAS_INIT_ITEM_ID, -1L);
    }

    //if (mInitItemId == -1L) {
    //  finish();
    //}
  }

  @Override protected void onResume() {
    super.onResume();
    if (mAdapter.getCount() > 0) {
      mResults.setCurrentItem(0);
    }

    if (!Utils.isEmpty(mItems)) {
      mResults.postDelayed(new Runnable() {
        @Override public void run() {
          for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).createdAt.equals(mInitItemId)) {
              mResults.setCurrentItem(i, true);
              break;
            }
          }
        }
      }, 500);
    }
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

  @Override public void deleteItem(ResultItem item) {
    final ResultItem dbItem =
        mItems.where().equalTo(ResultItem.KEY_CREATED_AT, item.createdAt).findFirst();
    if (dbItem != null) {
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override public void execute(Realm realm) {
          dbItem.removeFromRealm();
          Toast.makeText(ReviewActivity.this, "Item removed!", Toast.LENGTH_SHORT).show();
          mAdapter.notifyDataSetChanged();
        }
      });
    }
  }

  private static class ResultsPagerAdapter extends FragmentStatePagerAdapter {

    private final RealmResults<ResultItem> mItems;

    public ResultsPagerAdapter(FragmentManager fm, RealmResults<ResultItem> items) {
      super(fm);
      this.mItems = items;
    }

    @Override public int getCount() {
      return mItems == null ? 0 : mItems.size();
    }

    @Override public int getItemPosition(Object object) {
      return POSITION_NONE; // re-create after data change
    }

    @Override public Fragment getItem(int position) {
      ResultItem item = mItems.get(position);
      return DetailFragment.newInstance(item.createdAt);
    }
  }
}
