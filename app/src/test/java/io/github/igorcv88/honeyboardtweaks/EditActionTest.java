package io.github.igorcv88.honeyboardtweaks;

import android.view.inputmethod.InputConnection;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class EditActionTest {
    @Test public void contextButtonsDispatchExactlyOneNativeCommand() {
        for (EditAction action : new EditAction[]{EditAction.SELECT_ALL, EditAction.COPY, EditAction.CUT, EditAction.PASTE}) {
            List<Integer> commands = new ArrayList<>();
            InputConnection connection = (InputConnection) Proxy.newProxyInstance(
                    InputConnection.class.getClassLoader(), new Class<?>[]{InputConnection.class},
                    (proxy, method, args) -> {
                        assertEquals("performContextMenuAction", method.getName());
                        commands.add((int) args[0]); return true;
                    });
            assertTrue(action.execute(connection));
            assertEquals(List.of(action.command), commands);
        }
    }
    @Test public void disconnectedEditorIsHandled() {
        for (EditAction action : EditAction.values()) assertFalse(action.execute(null));
    }
}
