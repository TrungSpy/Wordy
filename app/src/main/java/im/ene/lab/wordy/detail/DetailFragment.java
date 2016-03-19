package im.ene.lab.wordy.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import im.ene.lab.wordy.R;
import im.ene.lab.wordy.WordyApp;
import im.ene.lab.wordy.result.ResultItem;
import io.realm.Realm;

/**
 * Created by eneim on 3/19/16.
 */
public class DetailFragment extends Fragment {

  private Long itemId;
  private ResultItem mItem;
  private Realm mRealm;

  public static final String ARGS_ITEM_ID = "args_item_detail_id";

  public static DetailFragment newInstance(Long itemId) {
    DetailFragment fragment = new DetailFragment();
    Bundle args = new Bundle();
    args.putLong(ARGS_ITEM_ID, itemId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      itemId = getArguments().getLong(ARGS_ITEM_ID);
    }

    if (itemId == null) {
      throw new IllegalArgumentException("Invalid ID");
    }

    mRealm = WordyApp.realm();
    mItem = mRealm.where(ResultItem.class).equalTo(ResultItem.KEY_CREATED_AT, itemId).findFirst();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.item_detail, container, false);
  }

  @Bind(R.id.item_image) ImageView mImage;

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Glide.with(this).load(mItem.fileUri).centerCrop().into(mImage);
  }
}
