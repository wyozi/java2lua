package wyozi.java2lua.asm;

import com.google.common.base.Joiner;
import org.objectweb.asm.*;
import wyozi.java2lua.core.LuaFunction;
import wyozi.java2lua.core.UnpackParameters;
import wyozi.java2lua.util.AsmUtil;
import wyozi.java2lua.util.LuaOutputter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
        public String getPushStack(String cst) {
            return "_stack[" + (++stackPointer) + "] = " + cst;
        }
        public void pushStack(String cst) {
            outputter.writeStatement(getPushStack(cst));
        }

        public String getPeekStack() {
            return "_stack[" + (stackPointer) + "]";
        }
        public String getPopStack() {
            try {
                return getPeekStack();
            } finally {
                stackPointer--;
            }
        }

        public void setLocal(int i, String cst) {
            outputter.writeStatement("_locals[" + i + "] = " + cst);
        }

        public String getLocalGetter(int i) {
            return "_locals[" + i + "]";
        }

        @Override
        public void visitLdcInsn(Object cst) {
            if (cst instanceof String)
                pushStack(String.format("\"%s\"", (String) cst));
            else
                pushStack(cst.toString());
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (opcode == Opcodes.NEW) {
                if ("java/lang/StringBuilder".equals(type)) {
                    pushStack("{}");
                }
            } else if (opcode == Opcodes.ANEWARRAY) {
                pushStack("{}");
            }
        }

        private List<Label> labelz = new ArrayList<Label>();
        private int getLabelNumber(Label label) {
            int idx = labelz.indexOf(label);
            if (idx != -1) return idx;

            labelz.add(label);
            return labelz.size()-1;
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode >= Opcodes.ICONST_0 && opcode <= Opcodes.ICONST_5) {
                pushStack(Integer.toString(opcode - Opcodes.ICONST_0));
            } else if (opcode == Opcodes.DCONST_0) {
                pushStack(Double.toString(0.0));
            } else if (opcode == Opcodes.DCONST_1) {
                pushStack(Double.toString(1.0));
            } else if (opcode == Opcodes.DUP) {
                pushStack(getPeekStack());
            } else if (opcode == Opcodes.IADD || opcode == Opcodes.DADD) {
                pushStack(getPopStack() + " + " + getPopStack());
            } else if (opcode == Opcodes.DCMPG) {
                String inlined = "(function(x, y) if x == y then return 0 elseif x >= y then return -1 else return 1 end end)(" +
                        getPopStack() + ", " + getPopStack() + ")";

                pushStack(inlined);
            } else if (opcode == Opcodes.AASTORE) {
                // arr idx val
                String val = getPopStack();
                String idx = getPopStack();
                String arr = getPopStack();
                outputter.writeStatement(arr + "[1 + " + idx + "] = " + val);
            }
        }

        @Override
        public void visitLabel(Label label) {
            outputter.writeStatement("::l" + getLabelNumber(label) + "::");
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            if (opcode == Opcodes.BIPUSH) {
                pushStack(Integer.toString(operand));
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if (opcode == Opcodes.ISTORE || opcode == Opcodes.DSTORE) {
                setLocal(var, getPopStack());
            }
            else if (opcode == Opcodes.ILOAD || opcode == Opcodes.DLOAD) {
                pushStack(getLocalGetter(var));
            }
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
            } else if (opcode == Opcodes.IFGE) {
                outputter.writeStatement("if " + getPopStack() + " >= 0 then goto l" + getLabelNumber(label) + " end");
            }
        }

        public Field findField(String owner, String name, String desc) {
            try {
                Class clz = getClass().getClassLoader().loadClass(owner.replace("/", "."));
                return clz.getField(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (opcode == Opcodes.GETSTATIC) {
                Field f = findField(owner, name, desc);
                String luaFuncName = (f.getAnnotation(LuaFunction.class) != null ? f.getAnnotation(LuaFunction.class).value() : f.getName());
                pushStack(luaFuncName);
            }
        }

        public Method findMethod(String owner, String name, String desc) {
            try {
                Class clz = getClass().getClassLoader().loadClass(owner.replace("/", "."));

                Type[] typez = Type.getArgumentTypes(desc);
                Class[] params = new Class[typez.length];
                for (int i = 0;i < typez.length; i++) {
                    params[i] = AsmUtil.toClass(typez[i]);
                }

                return clz.getMethod(name, params);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        private String getMethodCall(Method m) {
            boolean unpackParams = (m.getAnnotation(UnpackParameters.class) != null);
            String luaFuncName = (m.getAnnotation(LuaFunction.class) != null ? m.getAnnotation(LuaFunction.class).value() : m.getName());

            String[] params = new String[m.getParameterTypes().length];
            for (int i = 0;i < params.length; i++) {
                params[i] = getPopStack();
                if (unpackParams) params[i] = String.format("table.unpack(%s)", params[i]);
            }

            String methodCall = String.format("%s(%s)", luaFuncName, Joiner.on(", ").join(params));
            if (m.getReturnType() == void.class) {
                return methodCall;
            } else {
                return getPushStack(methodCall);
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESTATIC) {
                Method m = findMethod(owner, name, desc);

                // If auto-boxing, skip
                if ("valueOf".equals(name) && m.getParameterTypes()[0] != String.class) {
                    return;
                }

                outputter.writeStatement(getMethodCall(m));
            }
            else if (opcode == Opcodes.INVOKESPECIAL) {
                getPopStack();
                // TODO
            }
            else if (opcode == Opcodes.INVOKEVIRTUAL) {
                if ("java/lang/StringBuilder".equals(owner)) {
                    if ("append".equals(name)) {
                        String arg = getPopStack();
                        String ownerObj = getPeekStack(); // technically supposed to pop and re-push, but OPTIMUZATIONS
                        outputter.writeStatement("table.insert(%s, %s)", ownerObj, arg);
                    } else if ("toString".equals(name)) {
                        String ownerObj = getPopStack();
                        pushStack(String.format("table.concat(%s, \"\")", ownerObj));
                    }

                    return;
                }
                outputter.writeStatement(String.format("%s:%s", getPopStack(), getMethodCall(findMethod(owner, name, desc))));
            }
        }

        @Override
        public void visitEnd() {
            outputter.writeStatement("end");
        }
    }
}
