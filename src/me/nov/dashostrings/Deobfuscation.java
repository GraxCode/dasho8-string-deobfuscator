package me.nov.dashostrings;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.lpk.analysis.Sandbox;
import me.lpk.util.AccessHelper;
import me.nov.dashostrings.utils.InstructionUtils;
import me.nov.dashostrings.utils.DashOUtils;

public class Deobfuscation implements Opcodes {

  private Map<String, ClassNode> classes;
  private boolean success;
  public final static boolean SCND_METHOD = true;
  public final static boolean REMOVE_STATICINVK = true;

  public Deobfuscation(Map<String, ClassNode> classes) {
    this.classes = classes;
    this.success = false;
  }

  public boolean isSuccess() {
    return success;
  }

  public void start() {
    try {
      PrintWriter pw = new PrintWriter("strings.txt");
      for (ClassNode cn : classes.values()) {
        for (MethodNode mn : cn.methods) {
          for (AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain.getOpcode() == INVOKESTATIC) {
              MethodInsnNode min = (MethodInsnNode) ain;
              if (min.desc.equals("(Ljava/lang/String;I)Ljava/lang/String;") || min.desc.equals("(ILjava/lang/String;)Ljava/lang/String;")) {
                //additional check, you may need to change
                if (min.name.equals("insert") || min.name.equals("subSequence")) {
                  String s = deobfuscateString(mn, min);
                  if (s != null) {
                    mn.instructions.insertBefore(min, new InsnNode(POP2));
                    mn.instructions.set(min, new LdcInsnNode(s));
                  }
                }
              }
            }
          }
        }
      }
      success = true;
      pw.close();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private String deobfuscateString(MethodNode mn, MethodInsnNode min) {
    int nums = 0;
    String string = null;
    AbstractInsnNode ain = min;
    InsnList insn = new InsnList();
    while (string == null || nums < 2) {
      if (InstructionUtils.isNumber(ain) || ain.getOpcode() == DUP) {
        nums++;
      }
      if (ain instanceof LdcInsnNode) {
        string = ((LdcInsnNode) ain).cst.toString();
      }
      insn.insert(ain.clone(new HashMap<LabelNode, LabelNode>()));
      ain = ain.getPrevious();
      if (ain == null) {
        break;
      }
      if (ain instanceof LabelNode) {
        break;
      }
    }
    if (nums < 2 || string == null) {
      return null;
    }
    ClassNode invocationNode = DashOUtils.generateInvocation(classes, min, insn);
    Class<?> loaded = null;
    try {
      loaded = Sandbox.load(invocationNode);
      Method clinit = loaded.getMethod("decrypt_dasho");
      String s = clinit.invoke(null).toString(); // invoke decryption
      System.out.println(s);
      return s;
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    return null;
  }

}
