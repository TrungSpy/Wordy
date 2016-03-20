package im.ene.lab.wordy;

import android.content.DialogInterface;
import android.hardware.camera2.TotalCaptureResult;
import android.net.Uri;
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
import im.ene.lab.wordy.utils.ItemUnavailableException;
import im.ene.lab.wordy.utils.Utils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import java.io.File;
import org.threeten.bp.ZonedDateTime;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
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

    mAdapter = new ResultsAdapter(mRealm);
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
    final Long itemId = ZonedDateTime.now().toEpochSecond();
    final ResultItem item = new ResultItem(Uri.fromFile(file).getPath(), itemId);
    item.setState(ResultItem.STATE_INIT);
    // Create item in realm
    WordyApp.realm().executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        realm.copyToRealmOrUpdate(item);
      }
    });

    runOnUiThread(new Runnable() {
      @Override public void run() {
        mResults.scrollToPosition(0);
      }
    });

    // Request Alchemy API
    mSubscription = Observable.defer(new Func0<Observable<ImageKeywords>>() {
      @Override public Observable<ImageKeywords> call() {
        item.setState(ResultItem.STATE_UNKNOWN);
        // Create item in realm
        WordyApp.realm().executeTransaction(new Realm.Transaction() {
          @Override public void execute(Realm realm) {
            realm.copyToRealmOrUpdate(item);
          }
        });
        return Observable.just(alchemyVision.getImageKeywords(file, true, true));
      }
    }).flatMap(new Func1<ImageKeywords, Observable<ResultItem>>() {
      @Override public Observable<ResultItem> call(ImageKeywords imageKeywords) {
        // Bug: deleted item are found here. Have no idea ...
        if (WordyApp.realm()
            .where(ResultItem.class)
            .equalTo(ResultItem.KEY_CREATED_AT, item.createdAt)
            .findFirst() == null) {
          return Observable.error(new ItemUnavailableException("Item is unavailable any longer"));
        }

        item.setImageKeywords(imageKeywords.getImageKeywords());
        return Observable.just(item);
      }
    }).subscribeOn(Schedulers.io()).subscribe(new Action1<ResultItem>() {
      @Override public void call(final ResultItem item) {
        item.setState(ResultItem.STATE_SUCCESS);
        WordyApp.realm().executeTransaction(new Realm.Transaction() {
          @Override public void execute(Realm realm) {
            realm.copyToRealmOrUpdate(item);
          }
        });
      }
    }, new Action1<Throwable>() {
      @Override public void call(Throwable throwable) {
        if (throwable instanceof ItemUnavailableException) {
          return;
        }

        item.setState(ResultItem.STATE_FAILED);
        WordyApp.realm().executeTransaction(new Realm.Transaction() {
          @Override public void execute(Realm realm) {
            realm.copyToRealmOrUpdate(item);
          }
        });
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    // Retry
    final RealmResults<ResultItem> retryCache = mRealm.where(ResultItem.class)
        .notEqualTo("state", ResultItem.STATE_EDITED)
        .notEqualTo("state", ResultItem.STATE_SUCCESS)
        .findAll();

    if (!Utils.isEmpty(retryCache)) {
      // Update state to Unknown. We are re-trying.
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override public void execute(Realm realm) {
          for (int i = 0; i < retryCache.size(); i++) {
            ResultItem item = retryCache.get(i);
            item.setState(ResultItem.STATE_UNKNOWN);
            realm.copyToRealmOrUpdate(item);
          }
        }
      });
    }

    // Re-try request to Alchemy API
    Observable.from(retryCache).flatMap(new Func1<ResultItem, Observable<ResultItem>>() {
      @Override public Observable<ResultItem> call(ResultItem item) {
        // Convert from Realm Object to normal Object
        ResultItem resultItem = new ResultItem(item.filePath, item.createdAt);
        resultItem.setResult(item.result);
        return Observable.just(resultItem);
      }
    }).observeOn(Schedulers.io()).forEach(new Action1<ResultItem>() {
      @Override public void call(final ResultItem item /* In memory POJO, not realm object */) {
        // Request Alchemy API
        Observable.defer(new Func0<Observable<ImageKeywords>>() {
          @Override public Observable<ImageKeywords> call() {
            return Observable.just(
                alchemyVision.getImageKeywords(new File(item.filePath), true, true));
          }
        }).flatMap(new Func1<ImageKeywords, Observable<ResultItem>>() {
          @Override public Observable<ResultItem> call(ImageKeywords imageKeywords) {
            item.setImageKeywords(imageKeywords.getImageKeywords());
            return Observable.just(item);
          }
        }).subscribeOn(Schedulers.io()).subscribe(new Action1<ResultItem>() {
          @Override public void call(final ResultItem item) {
            item.setState(ResultItem.STATE_SUCCESS);
            WordyApp.realm().executeTransaction(new Realm.Transaction() {
              @Override public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
              }
            });
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable throwable) {
            item.setState(ResultItem.STATE_FAILED);
            WordyApp.realm().executeTransaction(new Realm.Transaction() {
              @Override public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
              }
            });
          }
        });
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
            mRealm.executeTransaction(new Realm.Transaction() {
              @Override public void execute(Realm realm) {
                item.removeFromRealm();
              }
            });
          }
        })
        .create()
        .show();
    return true;
  }

  @Override public void onChange() {
    Log.e(TAG, "onChange() called with: " + "");
    mAdapter.notifyDataSetChanged();
  }

  @Override public void onItemUpdated(ResultItem item) {
    // mAdapter.notifyDataSetChanged();
  }
}
