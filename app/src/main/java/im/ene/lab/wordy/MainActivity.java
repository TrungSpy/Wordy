package im.ene.lab.wordy;

import android.content.DialogInterface;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyVision;
import com.ibm.watson.developer_cloud.alchemy.v1.model.ImageKeywords;
import im.ene.lab.wordy.camera.Camera2BasicFragment;
import im.ene.lab.wordy.data.api.ApiService;
import im.ene.lab.wordy.detail.ReviewActivity;
import im.ene.lab.wordy.result.EditorDialogFragment;
import im.ene.lab.wordy.result.ResultItem;
import im.ene.lab.wordy.result.ResultsAdapter;
import im.ene.lab.wordy.utils.Utils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import java.io.File;
import java.util.Date;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
    implements Camera2BasicFragment.Callback, ResultsAdapter.OnItemLongClickListener,
    RealmChangeListener, EditorDialogFragment.Callback {

  private static final String TAG = "MainActivity";
  private final AlchemyVision alchemyVision = new AlchemyVision();

  private TextView mEmptyText;
  private RecyclerView mResults;
  private ResultsAdapter mAdapter;

  private Realm mRealm;
  private Subscription mSubscription;

  private ResultsAdapter.OnResultItemClickListener mClickListener =
      new ResultsAdapter.OnResultItemClickListener() {

        @Override public void openItemDetail(ResultItem item) {
          startActivity(ReviewActivity.createIntent(MainActivity.this, item));
        }

        @Override public void editItem(ResultItem item) {
          EditorDialogFragment dialogFragment = EditorDialogFragment.newInstance(item.createdAt);
          dialogFragment.show(getSupportFragmentManager(), EditorDialogFragment.TAG);
        }
      };

  private RecyclerView.AdapterDataObserver mDataChangeObserver =
      new RecyclerView.AdapterDataObserver() {
        @Override public void onChanged() {
          super.onChanged();
          onDataChanged();
        }

        @Override public void onItemRangeChanged(int positionStart, int itemCount) {
          super.onItemRangeChanged(positionStart, itemCount);
          onDataChanged();
        }

        @Override public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
          super.onItemRangeChanged(positionStart, itemCount, payload);
          onDataChanged();
        }

        @Override public void onItemRangeInserted(int positionStart, int itemCount) {
          super.onItemRangeInserted(positionStart, itemCount);
          onDataChanged();
        }

        @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
          super.onItemRangeMoved(fromPosition, toPosition, itemCount);
          onDataChanged();
        }

        @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
          super.onItemRangeRemoved(positionStart, itemCount);
          onDataChanged();
        }

        private void onDataChanged() {
          if (mAdapter.getItemCount() > 0) {
            mEmptyText.setVisibility(View.INVISIBLE);
          } else {
            mEmptyText.setVisibility(View.VISIBLE);
          }
        }
      };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    alchemyVision.setApiKey(ApiService.alchemyApiKey);

    mRealm = WordyApp.realm();
    mRealm.addChangeListener(this);

    mEmptyText = (TextView) findViewById(R.id.empty_view);
    mResults = (RecyclerView) findViewById(R.id.result);
    mResults.setLayoutManager(
        new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false));

    mAdapter = new ResultsAdapter();
    mAdapter.setItemClickListener(mClickListener);
    mAdapter.setItemLongClickListener(this);

    mResults.setAdapter(mAdapter);
    mAdapter.registerAdapterDataObserver(mDataChangeObserver);

    if (null == savedInstanceState) {
      getFragmentManager().beginTransaction()
          .replace(R.id.container, Camera2BasicFragment.newInstance())
          .commit();
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

  @Override public void onCaptured(TotalCaptureResult result, final File file) {
    final ResultItem item = new ResultItem(file.toURI().toString(), (new Date().getTime()), null);
    item.setState(ResultItem.STATE_UNKNOWN);

    runOnUiThread(new Runnable() {
      @Override public void run() {
        mAdapter.addItem(item);
        mResults.scrollToPosition(0);

        mRealm.executeTransaction(new Realm.Transaction() {
          @Override public void execute(Realm realm) {
            realm.copyToRealmOrUpdate(item);
          }
        });
      }
    });

    // after capture a photo
    mSubscription = Observable.defer(new Func0<Observable<ImageKeywords>>() {
      @Override public Observable<ImageKeywords> call() {
        ImageKeywords keywords = alchemyVision.getImageKeywords(file, true, true);
        return Observable.just(keywords);
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<ImageKeywords>() {
          @Override public void onCompleted() {
          }

          @Override public void onError(Throwable e) {
            item.setState(ResultItem.STATE_FAILED);
            mRealm.executeTransaction(new Realm.Transaction() {
              @Override public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
              }
            });
            mAdapter.updateItem(item);
          }

          @Override public void onNext(ImageKeywords recognizedImage) {
            Log.d(TAG, "onNext() called with: " + "recognizedImage = [" + recognizedImage + "]");
            item.setImageKeywords(recognizedImage.getImageKeywords());
            item.setState(ResultItem.STATE_SUCCESS);
            mRealm.executeTransaction(new Realm.Transaction() {
              @Override public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
              }
            });
            mAdapter.updateItem(item);
          }
        });
  }

  @Override protected void onPause() {
    if (mSubscription != null && !mSubscription.isUnsubscribed()) {
      mSubscription.unsubscribe();
    }
    super.onPause();
  }

  @Override protected void onDestroy() {
    if (mRealm != null) {
      mRealm.removeChangeListener(this);
      mRealm.close();
    }

    mAdapter.unregisterAdapterDataObserver(mDataChangeObserver);
    mClickListener = null;
    super.onDestroy();
  }

  @Override
  public boolean onItemLongClick(final ResultsAdapter parent, View view, final int position) {
    new AlertDialog.Builder(this).setMessage("Do you want to remove this image?")
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            final ResultItem item = parent.getItem(position);
            parent.removeItem(item);
            mRealm.executeTransaction(new Realm.Transaction() {
              @Override public void execute(Realm realm) {
                RealmResults<ResultItem> items = realm.where(ResultItem.class)
                    .equalTo(ResultItem.KEY_CREATED_AT, item.createdAt)
                    .findAll();
                if (!Utils.isEmpty(items)) {
                  items.clear();
                }
              }
            });
          }
        })
        .create()
        .show();
    return true;
  }

  @Override public void onChange() {
  }

  @Override public void onItemUpdated(ResultItem item) {
    mAdapter.updateItem(item);
  }
}
