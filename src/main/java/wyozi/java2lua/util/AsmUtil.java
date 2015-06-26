package wyozi.java2lua.util;

import org.objectweb.asm.Type;

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
        else if (c == 'L')
            return AsmUtil.class.getClassLoader().loadClass(t.getClassName());
        else
            throw new RuntimeException("Unhandled primitive type " + t.getDescriptor());
    }
}
