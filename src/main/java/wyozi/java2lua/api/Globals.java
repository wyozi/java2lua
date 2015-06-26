package wyozi.java2lua.api;

import wyozi.java2lua.core.LuaFunction;

/**
 * Created by Joonas on 26.6.2015.
 */
public class Globals {
    @LuaFunction("print")
    public static void print(Object arg) {
        System.out.println(arg);
    }
}
