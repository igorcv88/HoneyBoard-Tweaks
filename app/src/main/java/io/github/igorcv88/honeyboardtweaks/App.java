package io.github.igorcv88.honeyboardtweaks;

import android.app.Application;
import android.content.SharedPreferences;
import java.lang.ref.WeakReference;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

public final class App extends Application {
    volatile SharedPreferences prefs;
    WeakReference<MainActivity> screen = new WeakReference<>(null);
    @Override public void onCreate() {
        super.onCreate();
        XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
            @Override public void onServiceBind(XposedService service) {
                try { prefs = service.getRemotePreferences(Config.GROUP); }
                catch (RuntimeException error) { prefs = null; }
                refresh();
            }
            @Override public void onServiceDied(XposedService service) { prefs = null; refresh(); }
        });
    }
    private void refresh() {
        MainActivity activity = screen.get();
        if (activity != null) activity.runOnUiThread(activity::render);
    }
}
