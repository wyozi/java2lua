package wyozi.java2lua.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Joonas on 26.6.2015.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaFunction {
    public String value();
}
