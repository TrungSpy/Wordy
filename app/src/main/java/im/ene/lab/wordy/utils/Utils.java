package im.ene.lab.wordy.utils;

import android.text.TextUtils;
import java.util.List;

/**
 * Created by eneim on 3/19/16.
 */
public class Utils {

  private Utils() {

  }

  public static <T> boolean isEmpty(List<T> list) {
    return list == null || list.size() == 0;
  }

  public static boolean isEmpty(CharSequence sequence) {
    return sequence == null || TextUtils.isEmpty(sequence);
  }
}
