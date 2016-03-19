package im.ene.lab.wordy.result;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by eneim on 3/19/16.
 */
public class ResultsAdapter extends RecyclerView.Adapter<ResultItemViewHolder> {

  private final Object LOCK = new Object();

  private final SortedList<ResultItem> mSortedItems;

  private OnItemClickListener mItemClickListener;

  private OnItemLongClickListener mItemLongClickListener;

  public ResultsAdapter() {
    super();
    mSortedItems =
        new SortedList<>(ResultItem.class, new SortedListAdapterCallback<ResultItem>(this) {
          @Override public int compare(ResultItem o1, ResultItem o2) {
            return o2.createdAt.compareTo(o1.createdAt);
          }

          @Override public boolean areContentsTheSame(ResultItem oldItem, ResultItem newItem) {
            return newItem.fileUri.equals(oldItem.fileUri)
                && newItem.result.equals(oldItem.result)
                && newItem.state.equals(oldItem.state);
          }

          @Override public boolean areItemsTheSame(ResultItem item1, ResultItem item2) {
            return item1.fileUri.equals(item2.fileUri);
          }
        });
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

  public void updateItem(ResultItem item) {
    synchronized (LOCK) {
      mSortedItems.updateItemAt(mSortedItems.indexOf(item), item);
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
