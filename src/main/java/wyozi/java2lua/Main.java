package wyozi.java2lua;

import org.objectweb.asm.ClassReader;
import wyozi.java2lua.asm.LuafierVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Joonas on 26.6.2015.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        File cls = new File("target/classes/wyozi/java2lua/test/Test.class");
        ClassReader reader = new ClassReader(new FileInputStream(cls));
        LuafierVisitor luafierVisitor = new LuafierVisitor();
        reader.accept(luafierVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        System.out.println(luafierVisitor.getLua());

    }
}
