package io.github.igorcv88.honeyboardtweaks;

import android.content.Context;
import android.graphics.drawable.Icon;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import io.github.libxposed.api.XposedInterface.ExceptionMode;

/** Native Bee objects: Samsung owns rendering, toolbar sizing, customization and touch dispatch. */
final class NativeToolbar {
    private final HoneyboardModule module;
    private final ClassLoader loader;
    private final Class<?> bee, info, builder, tag;
    NativeToolbar(HoneyboardModule module, ClassLoader loader) throws Throwable {
        this.module = module; this.loader = loader;
        bee = Class.forName("ue.b", false, loader);
        info = Class.forName("ue.j", false, loader);
        builder = Class.forName("ue.j$a", false, loader);
        tag = Class.forName(Config.TARGET + ".common.beehive.BeeTag", false, loader);
    }
    void install() throws Throwable {
        // Validate all construction paths before installing any hook.
        Constructor<?> tagCtor = tag.getDeclaredConstructor(String.class, boolean.class);
        builder.getDeclaredConstructor(Context.class, Icon.class, int.class,
                android.content.res.Resources.class, String.class);
        info.getDeclaredConstructor(builder);
        for (String type : new String[]{"im.a", "im.b"}) {
            Class<?> clazz = Class.forName(type, false, loader);
            module.hook(Reflect.method(clazz, "d")).setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        List<?> original = (List<?>) chain.proceed();
                        return toolbarOrder(original, tagCtor);
                    });
        }
        Class<?> world = Class.forName(Config.TARGET + ".beehive.viewmodel.BeeWorld", false, loader);
        Method contextMethod = Reflect.method(world, "N");
        Method refresh = Reflect.method(world, "X0", List.class);
        module.hook(refresh).setExceptionMode(ExceptionMode.PROTECTIVE).intercept(chain -> {
            List<?> original = (List<?>) chain.getArg(0);
            List<Object> updated = new ArrayList<>();
            for (Object item : original) {
                String id = (String) Reflect.call(item, "getBeeId");
                if (!id.startsWith("hbt_")) updated.add(item);
            }
            Context context = (Context) contextMethod.invoke(chain.getThisObject());
            for (EditAction action : Config.actions(module.preferences()))
                updated.add(createBee(context, action));
            module.once("native toolbar refresh executed");
            return chain.proceed(new Object[]{updated});
        });
        // The mapped call site is in BeeHiveComponent, not BeeWorld itself.
        Class<?> component = Class.forName(Config.TARGET + ".beehive.component.BeeHiveComponent",
                false, loader);
        if (!module.deoptimize(Reflect.method(component, "G2")))
            module.status("toolbar refresh deoptimization unavailable");
    }
    private List<?> toolbarOrder(List<?> original, Constructor<?> ctor) throws Throwable {
        List<EditAction> enabled = Config.actions(module.preferences());
        Set<String> wanted = new HashSet<>(), found = new HashSet<>();
        for (EditAction a : enabled) wanted.add(a.beeId);
        List<Object> copy = new ArrayList<>();
        for (Object item : original) {
            String id = (String) Reflect.call(item, "getId");
            if (!id.startsWith("hbt_") || wanted.contains(id)) {
                copy.add(item); found.add(id);
            }
        }
        // New actions start in the main toolbar; saved native order wins on subsequent refreshes.
        int insertion = 0;
        for (EditAction a : enabled) {
            if (!found.contains(a.beeId)) copy.add(insertion++, ctor.newInstance(a.beeId, false));
        }
        return copy;
    }
    private Object createBee(Context context, EditAction action) throws Throwable {
        Context assets = context.createPackageContext(
                module.getModuleApplicationInfo().packageName, Context.CONTEXT_IGNORE_SECURITY);
        int iconId = assets.getResources().getIdentifier("ic_toolbar_" + action.key,
                "drawable", assets.getPackageName());
        int labelId = assets.getResources().getIdentifier(action.key, "string", assets.getPackageName());
        if (iconId == 0 || labelId == 0) throw new IllegalStateException("Missing action resources");
        Object b = builder.getDeclaredConstructor(Context.class, Icon.class, int.class,
                android.content.res.Resources.class, String.class).newInstance(
                context, Icon.createWithResource(assets, iconId), labelId,
                assets.getResources(), assets.getPackageName());
        Reflect.field(b, "f", action.label);
        Reflect.field(b, "g", labelId);
        Reflect.field(b, "h", action.label);
        Object details = info.getDeclaredConstructor(builder).newInstance(b);
        return Proxy.newProxyInstance(loader, new Class<?>[]{bee}, new ActionBee(module, action, details));
    }
}
