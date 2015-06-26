package wyozi.java2lua.api;

import wyozi.java2lua.core.LuaFunction;
import wyozi.java2lua.core.UnpackParameters;

/**
 * Created by Joonas on 26.6.2015.
 */
public class Globals {
    @LuaFunction("print")
    @UnpackParameters
    public static void print(Object... args) {
        System.out.println(args);
    }
}
