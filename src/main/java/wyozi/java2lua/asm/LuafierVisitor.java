package wyozi.java2lua.asm;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.objectweb.asm.*;
import wyozi.java2lua.core.LuaFunction;
import wyozi.java2lua.util.LuaOutputter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Joonas on 26.6.2015.
 */
public class LuafierVisitor extends ClassVisitor {
    private LuaOutputter outputter = new LuaOutputter();

    public LuafierVisitor() {
        super(Opcodes.ASM5);
        outputter.writeStatement("local _locals = {}");
    }

    public String getLua() {
        return outputter.getLua();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new LuafierMethodVisitor(access, name, desc, signature, exceptions);
    }

    public class LuafierMethodVisitor extends MethodVisitor {

        private int access;
        private String name;
        private String desc;
        private String signature;
        private String[] exceptions;

        public LuafierMethodVisitor(int access, String name, String desc, String signature, String[] exceptions) {
            super(Opcodes.ASM5);
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;

            outputter.writeComment("Starting method " + name + desc);
            outputter.writeStatement("do");
            outputter.writeStatement("local _stack = {}");
        }

        private int stackPointer = 0;
        public void pushStack(String cst) {
            outputter.writeStatement("_stack[" + (++stackPointer) + "] = " + cst);
        }

        public String getPopStack() {
            return "_stack[" + (stackPointer--) + "]";
        }

        public void setLocal(int i, String cst) {
            outputter.writeStatement("_locals[" + i + "] = " + cst);
        }

        public String getLocalGetter(int i) {
            return "_locals[" + i + "]";
        }

        @Override
        public void visitLdcInsn(Object cst) {
            pushStack(String.format("\"%s\"", (String) cst));
        }

        private List<Label> labelz = new ArrayList<Label>();
        private int getLabelNumber(Label label) {
            int idx = labelz.indexOf(label);
            if (idx != -1) return idx;

            labelz.add(label);
            return labelz.size()-1;
        }

        @Override
        public void visitLabel(Label label) {
            outputter.writeStatement("::l" + getLabelNumber(label) + "::");
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode >= Opcodes.ICONST_0 && opcode <= Opcodes.ICONST_5) {
                pushStack(Integer.toString(opcode - Opcodes.ICONST_0));
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if (opcode == Opcodes.ISTORE) {
                setLocal(var, getPopStack());
            }
            else if (opcode == Opcodes.ILOAD) {
                pushStack(getLocalGetter(var));
            }
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {

        }

        @Override
        public void visitIincInsn(int var, int increment) {
            setLocal(var, getLocalGetter(var) + " + " + increment);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (opcode == Opcodes.GOTO) {
                outputter.writeStatement("goto l" + getLabelNumber(label));
            }
            else if (opcode == Opcodes.IF_ICMPGE) {
                outputter.writeStatement("if " + getPopStack() + " <= " + getPopStack() + " then goto l" + getLabelNumber(label) + " end");
            }
        }

        public Method findMethod(String owner, String name, String desc) {
            try {
                Class clz = getClass().getClassLoader().loadClass(owner.replace("/", "."));

                Type[] typez = Type.getArgumentTypes(desc);
                Class[] params = new Class[typez.length];
                for (int i = 0;i < typez.length; i++)
                    params[i] = getClass().getClassLoader().loadClass(typez[i].getClassName());

                return clz.getMethod(name, params);
            } catch (ClassNotFoundException e) {
                return null;
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESTATIC) {
                Method m = findMethod(owner, name, desc);

                String luaFuncName = m.getAnnotation(LuaFunction.class).value();

                String[] params = new String[m.getParameterTypes().length];
                Arrays.fill(params, getPopStack());

                outputter.writeStatement(String.format("%s(%s)", luaFuncName, Joiner.on(", ").join(params)));
            }
            System.out.println(owner + name + desc);
        }

        @Override
        public void visitEnd() {
            outputter.writeStatement("end");
        }
    }
}
