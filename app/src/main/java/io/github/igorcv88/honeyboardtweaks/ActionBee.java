package io.github.igorcv88.honeyboardtweaks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

final class ActionBee implements InvocationHandler {
    private final HoneyboardModule module;
    private final EditAction action;
    private final Object info;
    private Object callback;
    private boolean sleeping;
    ActionBee(HoneyboardModule module, EditAction action, Object info) {
        this.module = module; this.action = action; this.info = info;
    }
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return switch (method.getName()) {
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> "HoneyBoardAction(" + action.key + ")";
            case "getBeeId" -> action.beeId;
            case "getBeeInfo" -> info;
            case "getBeeFlags" -> 1;
            case "getBeeVisibility", "getSearchSuggestionType" -> 0;
            case "getPinBeePriority" -> Integer.MAX_VALUE;
            case "isPpAccepted", "needLabel" -> true;
            case "getCallback" -> callback;
            case "setCallback" -> { callback = args[0]; yield null; }
            case "isSleep" -> sleeping;
            case "setSleep" -> { sleeping = (boolean) args[0]; yield null; }
            case "execute" -> {
                module.execute(action);
                if (args != null && args.length == 1 && args[0] != null)
                    Reflect.method(args[0].getClass(), "invoke").invoke(args[0]);
                yield null;
            }
            default -> {
                if (method.getReturnType() == boolean.class) yield false;
                if (method.getReturnType() == int.class) yield 0;
                yield null;
            }
        };
    }
}
