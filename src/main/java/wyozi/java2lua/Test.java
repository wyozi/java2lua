package wyozi.java2lua;

import wyozi.java2lua.api.Globals;
import wyozi.java2lua.core.Script;

/**
 * Created by Joonas on 26.6.2015.
 */
public class Test extends Script {
    public void run() {
        for (int i = 0;i < 4; i++) {
            Globals.print("Hello world!");
        }
    }
}
