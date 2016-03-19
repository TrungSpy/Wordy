package im.ene.lab.wordy.result;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.lab.wordy.R;

/**
 * Created by eneim on 3/19/16.
 */
public class ResultItemViewHolder extends RecyclerView.ViewHolder {

    static final int LAYOUT_RES = R.layout.result_item_view;

    public static ResultItemViewHolder createViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(LAYOUT_RES, parent, false);
        return new ResultItemViewHolder(view);
    }

    @Bind(R.id.result_image)
    ImageView mImage;
    @Bind(R.id.result_text)
    TextView mText;


    public ResultItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

    }

    public void bind(ResultItem item) {

    }
}
