package im.ene.lab.wordy.result;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 3/19/16.
 */
public class ResultsAdapter extends RecyclerView.Adapter<ResultItemViewHolder> {

    private final Object LOCK = new Object();

    private final List<ResultItem> mItems;

    private OnItemClickListener mItemClickListener;

    public ResultsAdapter() {
        super();
        mItems = new ArrayList<>();
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
            mItems.add(item);
            notifyItemInserted(mItems.size() - 1);
        }
    }

    @Override
    public ResultItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ResultItemViewHolder viewHolder = ResultItemViewHolder.createViewHolder(parent, viewType);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    @Override
    public void onBindViewHolder(ResultItemViewHolder holder, int position) {
        ResultItem item = getItem(position);
        holder.bind(item);
    }

    public final ResultItem getItem(int position) {
        if (position < 0 || position > mItems.size() - 1) {
            throw new IllegalArgumentException("Invalid position");
        }

        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ResultsAdapter parent, View view, int position);
    }
}
