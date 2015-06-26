package wyozi.java2lua.util;

import org.objectweb.asm.Type;

import java.util.Arrays;

/**
 * Created by Joonas on 26.6.2015.
 */
public class AsmUtil {
    public static Class<?> toClass(Type t) throws ClassNotFoundException {
        char c = t.getDescriptor().charAt(0);

        if (c == 'I')
            return int.class;
        else if (c == 'D')
            return double.class;

        return Class.forName(t.getDescriptor().replace('/', '.'));
    }
}
