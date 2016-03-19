package im.ene.lab.wordy.result;

import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import im.ene.lab.wordy.R;
import im.ene.lab.wordy.utils.Utils;

/**
 * Created by eneim on 3/19/16.
 */
public class ResultItemViewHolder extends RecyclerView.ViewHolder {

  static final int LAYOUT_RES = R.layout.result_item_view;

  public static ResultItemViewHolder createViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(LAYOUT_RES, parent, false);
    return new ResultItemViewHolder(view);
  }

  @Bind(R.id.result_image) ImageView mImage;
  @Bind(R.id.result_text) TextView mText;
  @Bind(R.id.result_text_container) View mTextContainer;

  public ResultItemViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  public void setOnClickListener(View.OnClickListener listener) {
    itemView.setOnClickListener(listener);
    mTextContainer.setOnClickListener(listener);
  }

  public void bind(ResultItem item) {
    if (Utils.isEmpty(item.result)) {
      switch (item.state) {
        case ResultItem.STATE_INIT:
          TextViewCompat.setTextAppearance(mText, R.style.TextAppearance_Wordy_Init);
          mText.setText("(^__^)");
          break;
        case ResultItem.STATE_UNKNOWN:
          TextViewCompat.setTextAppearance(mText, R.style.TextAppearance_Wordy_Unknown);
          mText.setText("Guessing,\nlet's wait...");
          break;
        case ResultItem.STATE_FAILED:
          TextViewCompat.setTextAppearance(mText, R.style.TextAppearance_Wordy_Failed);
          mText.setText("Something\nwrong :'(");
          break;
        case ResultItem.STATE_SUCCESS:
          TextViewCompat.setTextAppearance(mText, R.style.TextAppearance_Wordy_Success);
          mText.setText("Yay!!");
          break;
      }
    } else {
      if (item.state == ResultItem.STATE_EDITTED) {
        TextViewCompat.setTextAppearance(mText, R.style.TextAppearance_Wordy_Edited);
      } else if (item.state == ResultItem.STATE_SUCCESS) {
        TextViewCompat.setTextAppearance(mText, R.style.TextAppearance_Wordy_Success);
      }

      mText.setText(item.result);
    }

    Glide.with(itemView.getContext()).load(item.fileUri).fitCenter().into(mImage);
  }

  public void onAttachedToParent() {

  }

  public void onDetachedFromParent() {

  }
}
