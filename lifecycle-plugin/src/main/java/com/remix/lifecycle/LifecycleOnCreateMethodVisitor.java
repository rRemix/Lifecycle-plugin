package com.remix.lifecycle;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * created by Remix on 2019-09-06
 */
class LifecycleOnCreateMethodVisitor extends MethodVisitor {

  public LifecycleOnCreateMethodVisitor(MethodVisitor mv) {
    super(Opcodes.ASM5, mv);
    System.out.println("LifecycleOnCreateMethodVisitor");
  }

  @Override
  public void visitCode() {
    System.out.println("LifecycleOnCreateMethodVisitor visitCode");
    super.visitCode();

    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;",
        false);
    mv.visitMethodInsn(INVOKESTATIC, "timber/log/Timber", "tag",
        "(Ljava/lang/String;)Ltimber/log/Timber$Tree;",
        false);
    mv.visitLdcInsn("onCreate");
    mv.visitInsn(ICONST_0);
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    mv.visitMethodInsn(INVOKEVIRTUAL, "timber/log/Timber$Tree", "v",
        "(Ljava/lang/String;[Ljava/lang/Object;)V",
        false);
  }


}
