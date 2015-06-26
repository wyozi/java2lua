package wyozi.java2lua.util;

/**
 * Created by Joonas on 26.6.2015.
 */
public class LuaOutputter {
    private StringBuilder sb = new StringBuilder();

    public void writeComment(String com) {
        sb.append("--" + com);
        sb.append('\n');
    }

    public void writeStatement(String stmt) {
        sb.append(stmt);
        sb.append('\n');
    }
    public void writeStatement(String stmt, Object... args) {
        writeStatement(String.format(stmt, args));
    }

    public String getLua() {
        return sb.toString();
    }
}
