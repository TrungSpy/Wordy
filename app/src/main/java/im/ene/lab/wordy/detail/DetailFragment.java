package im.ene.lab.wordy.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import im.ene.lab.wordy.R;
import im.ene.lab.wordy.WordyApp;
import im.ene.lab.wordy.result.EditorDialogFragment;
import im.ene.lab.wordy.result.ResultItem;
import im.ene.lab.wordy.utils.Utils;
import io.realm.Realm;
import java.util.Locale;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * Created by eneim on 3/19/16.
 */
public class DetailFragment extends Fragment
    implements EditorDialogFragment.Callback, PopupMenu.OnMenuItemClickListener {

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

  private static final DateTimeFormatter YYYY_MM_DD_EEE =
      DateTimeFormatter.ofPattern("YYYY/MM/dd (EEE)", Locale.getDefault());

  @Bind(R.id.item_image) ImageView mImage;
  @Bind(R.id.detail_container) CardView mContainer;
  @Bind(R.id.item_text) TextView mItemText;
  @Bind(R.id.item_timestamp) TextView mTimeStamp;
  @Bind(R.id.button_actions) AppCompatImageButton mActions;

  @OnClick(R.id.button_actions) void actions() {
    PopupMenu popupMenu = new PopupMenu(getContext(), mActions);
    popupMenu.setOnMenuItemClickListener(DetailFragment.this);
    popupMenu.inflate(R.menu.item_detail_actions);
    popupMenu.show();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Glide.with(this).load(mItem.filePath).centerCrop().into(mImage);
    if (!Utils.isEmpty(mItem.result)) {
      mItemText.setText(mItem.result);
    } else {
      mItemText.setText("---");
    }

    String createdTime = YYYY_MM_DD_EEE.format(
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(mItem.createdAt), ZoneId.systemDefault()));
    mTimeStamp.setText("Created at: " + createdTime);
  }

  @Override public void onItemUpdated(ResultItem item) {
    if (!Utils.isEmpty(item.result)) {
      mItemText.setText(item.result);
    } else {
      mItemText.setText("---");
    }
  }

  private static final String TAG = "DetailFragment";

  @Override public boolean onMenuItemClick(MenuItem item) {
    int itemId = item.getItemId();
    switch (itemId) {
      case R.id.action_edit:
        EditorDialogFragment dialogFragment = EditorDialogFragment.newInstance(mItem.createdAt);
        dialogFragment.setTargetFragment(DetailFragment.this, 0);
        dialogFragment.show(getChildFragmentManager(), EditorDialogFragment.TAG);
        return true;
      case R.id.action_delete:
        if (mCallback != null) {
          mCallback.deleteItem(mItem);
          return true;
        } else {
          return false;
        }
      default:
        return false;
    }
  }

  private Callback mCallback;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof Callback) {
      mCallback = (Callback) context;
    }
  }

  @Override public void onDetach() {
    mCallback = null;
    super.onDetach();
  }

  public interface Callback {

    void deleteItem(ResultItem item);
  }
}
