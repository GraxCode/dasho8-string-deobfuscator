package me.nov.dashostrings.utils;

import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.util.OpUtils;

public class DashOUtils implements Opcodes {

  private static MethodNode cloneMethod(MethodNode mn) {
    MethodNode mn2 = new MethodNode(mn.access, mn.name, mn.desc, null, null); // construct
    mn2.instructions = MethodUtils.clone(mn.instructions, null);
    mn2.localVariables = null;
    mn2.maxLocals = mn.maxLocals;
    mn2.maxStack = mn.maxStack;
    return mn2;
  }

  public static ClassNode generateInvocation(Map<String, ClassNode> classes, MethodInsnNode orig, InsnList insn) {
    for(AbstractInsnNode ain : insn.toArray()) {
      if(ain.getOpcode() == INVOKESTATIC) {
        MethodInsnNode min = (MethodInsnNode) ain;
        min.owner = "dasho_obfuscated";
      }
    }
    //add return
    insn.add(new InsnNode(ARETURN));
    ClassNode decryptNode = new ClassNode();
    decryptNode.name = "dasho_obfuscated";
    decryptNode.superName = "java/lang/Object";
    decryptNode.version = 49;
    decryptNode.access = 1;
    MethodNode decrypt = new MethodNode(ACC_PUBLIC | ACC_STATIC, "decrypt_dasho", "()Ljava/lang/String;", null, null); // construct
    decrypt.instructions = insn;
    decrypt.localVariables = null;
    decrypt.maxLocals = 3;
    decrypt.maxStack = 3;
    decryptNode.methods.add(decrypt);
    decryptNode.methods.add(cloneMethod(MethodUtils.getMethod(classes.get(orig.owner), orig.name, orig.desc)));
    return decryptNode;
  }
}
