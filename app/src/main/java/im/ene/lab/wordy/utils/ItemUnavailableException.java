package im.ene.lab.wordy.utils;

/**
 * Created by eneim on 3/20/16.
 */
public class ItemUnavailableException extends Exception {

  public ItemUnavailableException() {
    super();
  }

  public ItemUnavailableException(String detailMessage) {
    super(detailMessage);
  }

  public ItemUnavailableException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  public ItemUnavailableException(Throwable throwable) {
    super(throwable);
  }
}
