package wyozi.java2lua;

import wyozi.java2lua.api.Globals;
import wyozi.java2lua.api.LuaMath;
import wyozi.java2lua.core.Script;

/**
 * Created by Joonas on 26.6.2015.
 */
public class Test extends Script {
    public void run() {
        Globals.print("Pi equals " + LuaMath.PI);
        for (double x = 0; x < 10; x += LuaMath.PI) {
            Globals.print("x: " + x + "; sin: " + LuaMath.sin(x) + "; cos: " + LuaMath.cos(x));
        }
    }
}
