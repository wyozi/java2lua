package wyozi.java2lua.api;

import wyozi.java2lua.core.LuaFunction;

/**
 * Created by Joonas on 26.6.2015.
 */
public class LuaMath {
    // Not final because compiler inlines it if its final
    @LuaFunction("math.pi")
    public static double PI = Math.PI;

    @LuaFunction("math.sin")
    public static double sin(double n) {
        return Math.sin(n);
    }
    @LuaFunction("math.cos")
    public static double cos(double n) {
        return Math.cos(n);
    }
}
