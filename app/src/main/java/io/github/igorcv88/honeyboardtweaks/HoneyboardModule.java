package io.github.igorcv88.honeyboardtweaks;

import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedInterface.ExceptionMode;

public final class HoneyboardModule extends XposedModule {
    static final String TAG = "HoneyBoardTweaks";
    private SharedPreferences prefs;
    private WeakReference<InputMethodService> ime = new WeakReference<>(null);
    private final Set<String> reported = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private volatile boolean supported;

    @Override public void onModuleLoaded(ModuleLoadedParam param) {
        if ((getFrameworkProperties() & PROP_CAP_REMOTE) != 0) prefs = getRemotePreferences(Config.GROUP);
        status(prefs == null ? "module loaded: remote preferences unavailable; hooks disabled" : "module loaded");
    }
    @Override public void onPackageLoaded(PackageLoadedParam param) {
        if (!Config.TARGET.equals(param.getPackageName())) return;
        try {
            // Obfuscated adapters are restricted to the exact analyzed base APK.
            java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
            try (java.io.InputStream input = new java.io.FileInputStream(param.getApplicationInfo().sourceDir)) {
                byte[] buffer = new byte[65536];
                for (int read; (read = input.read(buffer)) != -1;) sha.update(buffer, 0, read);
            }
            StringBuilder digest = new StringBuilder(64);
            for (byte value : sha.digest()) {
                digest.append(Character.forDigit((value & 255) >>> 4, 16));
                digest.append(Character.forDigit(value & 15, 16));
            }
            supported = digest.toString().equals(
                    "6a011ff31270a53dcc53b891009956a13f4de1ba3d271a6ca4316a6215fae840");
            status(supported ? "DZIF APK identity matched" : "unsupported APK: hooks disabled");
        } catch (Throwable error) { failure("APK identity", error); }
    }
    @Override public void onPackageReady(PackageReadyParam param) {
        if (!Config.TARGET.equals(param.getPackageName()) || !supported || prefs == null) return;
        ClassLoader loader = param.getClassLoader();
        install("IME lifecycle", () -> lifecycle(loader));
        install("native toolbar", () -> new NativeToolbar(this, loader).install());
        install("clipboard policy", () -> clipboard(loader));
        install("hidden settings", () -> hiddenSettings(loader));
    }
    interface Installation { void run() throws Throwable; }
    private void install(String feature, Installation installation) {
        try { installation.run(); status(feature + " hook installed"); }
        catch (Throwable error) { failure(feature, error); }
    }
    private void lifecycle(ClassLoader loader) throws Throwable {
        Class<?> service = Class.forName(Config.TARGET + ".service.HoneyBoardService", false, loader);
        Method start = Reflect.method(service, "onStartInputView", EditorInfo.class, boolean.class);
        hook(start).setExceptionMode(ExceptionMode.PROTECTIVE).intercept(chain -> {
            Object result = chain.proceed();
            ime = new WeakReference<>((InputMethodService) chain.getThisObject());
            once("IME lifecycle executed"); return result;
        });
        Method destroy = Reflect.method(service, "onDestroy");
        hook(destroy).setExceptionMode(ExceptionMode.PROTECTIVE).intercept(chain -> {
            if (ime.get() == chain.getThisObject()) ime.clear();
            return chain.proceed();
        });
    }
    private void clipboard(ClassLoader loader) throws Throwable {
        Class<?> repo = Class.forName("u1.f0", false, loader);
        Class<?> clip = Class.forName(Config.TARGET + ".icecone.clipboard.data.entity.Clip", false, loader);
        Class<?> store = Class.forName("k2.r", false, loader);
        Class<?> dao = Class.forName("k2.a", false, loader);
        Method query = Reflect.method(dao, "a", int.class);
        Method remove = Reflect.method(store, "a", long.class);
        Method trim = Reflect.method(repo, "q", repo, clip);
        Method notify = Reflect.method(repo, "i", int.class);
        // q is called by the insertion coroutine, so all DAO work stays off the UI thread.
        hook(trim).setExceptionMode(ExceptionMode.PROTECTIVE).intercept(chain -> {
            if (!Config.enabled(prefs, "clipboard", true)) return chain.proceed();
            Object repository = chain.getArg(0), incoming = chain.getArg(1);
            Object storage = Reflect.fieldOfType(repository, store);
            Object access = Reflect.fieldOfType(storage, dao);
            if (access == null) return chain.proceed();
            int user = (int) Reflect.call(incoming, "getUserId");
            List<?> clips = (List<?>) query.invoke(access, user);
            if (clips == null) return chain.proceed();
            List<ClipboardPolicy.Entry> entries = new ArrayList<>();
            for (Object item : clips) entries.add(entry(item));
            List<Long> victims = ClipboardPolicy.victims(entries, entry(incoming),
                    Config.capacity(prefs), Config.enabled(prefs, "keep_history", true));
            for (long id : victims) remove.invoke(storage, id);
            if (!victims.isEmpty()) notify.invoke(repository, user);
            once("clipboard policy executed"); return null;
        });
        // Prevent precompiled insertion code from bypassing q.
        Class<?> insertion = Class.forName("u1.i", false, loader);
        if (!deoptimize(Reflect.method(insertion, "invokeSuspend", Object.class)))
            status("clipboard insertion deoptimization unavailable");
    }
    private static ClipboardPolicy.Entry entry(Object clip) throws Throwable {
        return new ClipboardPolicy.Entry((long) Reflect.call(clip, "getId"),
                (long) Reflect.call(clip, "getTimestamp"), (int) Reflect.call(clip, "getUserId"),
                (int) Reflect.call(clip, "getOrigin"), (boolean) Reflect.call(clip, "getPinned"));
    }
    private void hiddenSettings(ClassLoader loader) throws Throwable {
        Class<?> fragment = Class.forName(Config.TARGET +
                ".settings.common.CommonSettingsFragmentCompat", false, loader);
        hook(Reflect.method(fragment, "isPreferenceVisible", String.class))
                .setExceptionMode(ExceptionMode.PROTECTIVE).intercept(chain -> {
                    Object original = chain.proceed();
                    if (Config.showSetting(prefs, (String) chain.getArg(0))) {
                        once("hidden settings executed"); return true;
                    }
                    return original;
                });
    }
    SharedPreferences preferences() { return prefs; }
    void execute(EditAction action) {
        InputMethodService service = ime.get();
        if (service == null || !Config.actions(prefs).contains(action)) return;
        try {
            boolean accepted = action.execute(service.getCurrentInputConnection());
            once("toolbar action executed: " + action.key);
            if (!accepted) android.widget.Toast.makeText(service,
                    action.label + " is not supported by this editor", android.widget.Toast.LENGTH_SHORT).show();
        } catch (Throwable error) { failure("toolbar action " + action.key, error); }
    }
    void status(String message) { log(Log.INFO, TAG, message); }
    void once(String message) { if (reported.add(message)) status(message); }
    void failure(String stage, Throwable error) {
        // Exceptions can contain editor/clipboard payloads; only log their type.
        log(Log.ERROR, TAG, stage + ": " + error.getClass().getSimpleName());
    }
}
