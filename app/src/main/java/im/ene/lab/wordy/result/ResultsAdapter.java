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

  public ResultsAdapter() {
    super();
    mSortedItems =
        new SortedList<>(ResultItem.class, new SortedListAdapterCallback<ResultItem>(this) {
          @Override public int compare(ResultItem o1, ResultItem o2) {
            return o2.fileUri.compareTo(o1.fileUri);
          }

          @Override public boolean areContentsTheSame(ResultItem oldItem, ResultItem newItem) {
            return newItem.fileUri.equals(oldItem.fileUri) && newItem.getImageKeywords()
                .equals(oldItem.getImageKeywords());
          }

          @Override public boolean areItemsTheSame(ResultItem item1, ResultItem item2) {
            return item1.fileUri.equals(item2.fileUri);
          }
        });
  }

  public ResultsAdapter(OnItemClickListener clickListener) {
    this();
    this.mItemClickListener = clickListener;
  }

  public void setItemClickListener(OnItemClickListener clickListener) {
    this.mItemClickListener = clickListener;
  }

  public void addItem(ResultItem item) {
    synchronized (LOCK) {
      mSortedItems.add(item);
    }
  }

  public void updatItem(ResultItem item) {
    synchronized (LOCK) {
      mSortedItems.updateItemAt(mSortedItems.indexOf(item), item);
    }
  }

  @Override public ResultItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final ResultItemViewHolder viewHolder = ResultItemViewHolder.createViewHolder(parent, viewType);
    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
          if (mItemClickListener != null) {
            mItemClickListener.onItemClick(ResultsAdapter.this, v, position);
          }
        }
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
    void onItemClick(ResultsAdapter parent, View view, int position);
  }
}
