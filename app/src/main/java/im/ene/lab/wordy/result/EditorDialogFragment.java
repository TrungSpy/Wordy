package im.ene.lab.wordy.result;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import im.ene.lab.wordy.R;
import im.ene.lab.wordy.WordyApp;
import im.ene.lab.wordy.utils.Utils;
import io.realm.Realm;

/**
 * Created by eneim on 3/19/16.
 */
public class EditorDialogFragment extends DialogFragment {

  public static final String TAG = "EditorDialogFragment";

  private Long itemId;
  private ResultItem mItem;
  private Realm mRealm;

  public static final String ARGS_ITEM_ID = "args_item_detail_id";

  public static EditorDialogFragment newInstance(Long itemId) {
    EditorDialogFragment fragment = new EditorDialogFragment();
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
    return inflater.inflate(R.layout.fragment_dialog_editor, container, false);
  }

  @Bind(R.id.item_image) ImageView mImage;
  @Bind(R.id.item_old_content) TextView mOldContent;
  @Bind(R.id.edit_item_content) TextInputEditText mEditText;
  @Bind(R.id.editor_cancel) Button mBtnCancel;
  @Bind(R.id.editor_submit) Button mBtnSubmit;

  @OnClick(R.id.editor_cancel) void cancel() {
    dismissAllowingStateLoss();
  }

  @OnClick(R.id.editor_submit) void submit() {
    final CharSequence newContent = mEditText.getText();
    if (!Utils.isEmpty(newContent)) {
      mRealm.executeTransaction(new Realm.Transaction() {
        @Override public void execute(Realm realm) {
          mItem.setResult(newContent.toString());
          realm.copyToRealmOrUpdate(mItem);
          if (mCallback != null) {
            mCallback.onItemUpdated(mItem);
          }
          dismissAllowingStateLoss();
        }
      });
    }
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Dialog dialog = getDialog();

    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
    DisplayMetrics metrics = getResources().getDisplayMetrics();

    int maxWidth = getResources().getDimensionPixelSize(R.dimen.dialog_max_width);
    int maxHeight = getResources().getDimensionPixelSize(R.dimen.dialog_max_height);

    lp.width = Math.min(maxWidth, metrics.widthPixels);
    lp.height = Math.min(maxHeight, metrics.heightPixels);
    dialog.getWindow().setAttributes(lp);

    Glide.with(this).load(mItem.fileUri).centerCrop().into(mImage);
    if (!Utils.isEmpty(mItem.result)) {
      mOldContent.setText(mItem.result);
    } else {
      mOldContent.setText("Nothing here yet!");
    }
  }

  private Callback mCallback;

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (activity instanceof Callback) {
      mCallback = (Callback) activity;
    }
  }

  @Override public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    mCallback = null;
  }

  @Override public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    mCallback = null;
  }

  @Override public void onDetach() {
    if (mRealm != null) {
      mRealm.close();
    }
    mCallback = null;
    super.onDetach();
  }

  public interface Callback {

    void onItemUpdated(ResultItem item);
  }
}
