package com.remix.lifecycle;


import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * created by Remix on 2019-09-06
 */
public class LifecycleVisitor extends ClassVisitor {
  private String mClassName;

  public LifecycleVisitor(ClassWriter classWriter) {
    super(Opcodes.ASM5, classWriter);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    System.out.println("LifecycleClassVisitor: visit --------> start: " + name);
    this.mClassName = name;
    super.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    System.out.println("LifecycleClassVisitor, visitMethod: " + name + " --------> start");

    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
    if (ACTIVITY_NAMES.contains(mClassName)) {
      if (name.equals("onCreate")) {
        System.out.println("LifecycleClassVisitor, find onCreate");
        return new LifecycleOnCreateMethodVisitor(mv);
      } else if (name.equals("onDestroy")) {
        System.out.println("LifecycleClassVisitor, find onDestroy");
        return new LifecycleOnDestroyMethodVisitor(mv);
      }
    }
    return mv;
  }

  @Override
  public void visitEnd() {
    System.out.println("LifecycleClassVisitor: visit --------> end");
    super.visitEnd();
  }

  public static final List<String> ACTIVITY_NAMES = Arrays.asList(
      "androidx/fragment/app/FragmentActivity",
      "android/support/v4/app/FragmentActivity");
}
