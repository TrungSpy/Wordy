package im.ene.lab.wordy;

import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by eneim on 3/19/16.
 */
public class WordyApp extends Application {

  public static Realm realm() {
    return Realm.getInstance(configuration);
  }

  private static RealmConfiguration configuration;

  @Override public void onCreate() {
    super.onCreate();

    configuration =
        new RealmConfiguration.Builder(this).name("wordy.realm")
            .deleteRealmIfMigrationNeeded()
            .schemaVersion(42).build();
  }
}
