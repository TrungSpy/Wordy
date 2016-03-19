package im.ene.lab.wordy.result;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import org.threeten.bp.ZonedDateTime;

/**
 * Created by eneim on 3/19/16.
 */
public class ResultsAdapter extends RecyclerView.Adapter<ResultItemViewHolder> {

  private final Object LOCK = new Object();

  private final Realm mRealm;
  private final RealmResults<ResultItem> mSortedItems;

  private OnItemClickListener mItemClickListener;

  private OnItemLongClickListener mItemLongClickListener;

  public ResultsAdapter(Realm realm) {
    super();
    mRealm = realm;
    mSortedItems = mRealm.where(ResultItem.class)
        .greaterThanOrEqualTo(ResultItem.KEY_CREATED_AT, ZonedDateTime.now().toEpochSecond())
        .findAllSorted(ResultItem.KEY_CREATED_AT, Sort.DESCENDING);
  }

  public void setItemClickListener(OnItemClickListener clickListener) {
    this.mItemClickListener = clickListener;
  }

  public void setItemLongClickListener(OnItemLongClickListener longClickListener) {
    this.mItemLongClickListener = longClickListener;
  }

  public void addItem(ResultItem item) {
    synchronized (LOCK) {
      mSortedItems.add(item);
    }
  }

  public void removeItem(ResultItem item) {
    synchronized (LOCK) {
      mSortedItems.remove(item);
    }
  }

  @Override public ResultItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final ResultItemViewHolder viewHolder = ResultItemViewHolder.createViewHolder(parent, viewType);
    viewHolder.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
          if (mItemClickListener != null) {
            mItemClickListener.onItemClick(ResultsAdapter.this, viewHolder, v, position);
          }
        }
      }
    });

    viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        if (mItemLongClickListener == null) {
          return false;
        }

        int position = viewHolder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
          return false;
        }

        return mItemLongClickListener.onItemLongClick(ResultsAdapter.this, v, position);
      }
    });
    return viewHolder;
  }

  @Override public void onBindViewHolder(ResultItemViewHolder holder, int position) {
    ResultItem item = getItem(position);
    holder.bind(item);
  }

  @Override public void onViewAttachedToWindow(ResultItemViewHolder holder) {
    super.onViewAttachedToWindow(holder);
    holder.onAttachedToParent();
  }

  @Override public void onViewDetachedFromWindow(ResultItemViewHolder holder) {
    super.onViewDetachedFromWindow(holder);
    holder.onDetachedFromParent();
  }

  public final ResultItem getItem(int position) {
    if (position < 0 || position > mSortedItems.size() - 1) {
      throw new IllegalArgumentException("Invalid position");
    }

    return mSortedItems.get(position);
  }

  @Override public int getItemCount() {
    return mSortedItems.size();
  }

  public interface OnItemClickListener {
    void onItemClick(ResultsAdapter parent, ResultItemViewHolder viewHolder, View view,
        int position);
  }

  public static abstract class OnResultItemClickListener implements OnItemClickListener {

    public abstract void openItemDetail(ResultItem item);

    public abstract void editItem(ResultItem item);

    @Override
    public void onItemClick(ResultsAdapter parent, ResultItemViewHolder viewHolder, View view,
        int position) {
      ResultItem item = parent.getItem(position);
      if (view == viewHolder.mTextContainer) {
        editItem(item);
      } else if (view == viewHolder.itemView) {
        openItemDetail(item);
      }
    }
  }

  public interface OnItemLongClickListener {
    boolean onItemLongClick(ResultsAdapter parent, View view, int position);
  }
}
