package me.nov.dashostrings.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MethodUtils implements Opcodes {
	public static MethodNode getClinit(ClassNode cn) {
		return getMethod(cn, "<clinit>");
	}

	public static boolean containsFieldName(ClassNode cn, String string) {
		for (FieldNode fn : cn.fields) {
			if (fn.name.equals(string)) {
				return true;
			}
		}
		return false;
	}

	public static AbstractInsnNode generateIntPush(int i) {
		if (i <= 5 && i >= -1) {
			return new InsnNode(i + 3); //iconst_i
		}
		if (i >= -128 && i <= 127) {
			return new IntInsnNode(BIPUSH, i);
		}

		if (i >= -32768 && i <= 32767) {
			return new IntInsnNode(SIPUSH, i);
		}
		return new LdcInsnNode(i);
	}

	public static int getIntValue(AbstractInsnNode node) {
		if (node.getOpcode() >= ICONST_M1 && node.getOpcode() <= ICONST_5) {
			return node.getOpcode() - 3;
		}
		if (node.getOpcode() == SIPUSH || node.getOpcode() == BIPUSH) {
			return ((IntInsnNode) node).operand;
		}
		throw new RuntimeException("node does not contains an integer!");
	}

	public static MethodNode getMethod(ClassNode cn, String name) {
		for (MethodNode mn : cn.methods) {
			if (mn.name.equals(name)) {
				return mn;
			}
		}
		return null;
	}

	public static MethodNode getMethod(ClassNode cn, String name, String desc) {
		for (MethodNode mn : cn.methods) {
			if (mn.name.equals(name) && mn.desc.equals(desc)) {
				return mn;
			}
		}
		return null;
	}
	
	public static InsnList clone(InsnList il, LabelNode end) { // TODO: invokedynamic
    HashMap<LabelNode, LabelNode> cloned = cloneLabels(il);
//    System.out.println(cloned.keySet().size());
    InsnList list = new InsnList();
    Loop:
    for (AbstractInsnNode ain : il.toArray()) {
      switch (ain.getType()) {
      case AbstractInsnNode.FIELD_INSN:
        FieldInsnNode fin = (FieldInsnNode) ain;
        list.add(new FieldInsnNode(ain.getOpcode(), fin.owner, fin.name, fin.desc));
        break;
      case AbstractInsnNode.FRAME:
        FrameNode fn = (FrameNode) ain;
        list.add(new FrameNode(fn.type, fn.local.size(), new ArrayList(fn.local).toArray(), fn.stack.size(),
            new ArrayList(fn.stack).toArray()));
        break;
      case AbstractInsnNode.IINC_INSN:
        IincInsnNode iinc = (IincInsnNode) ain;
        list.add(new IincInsnNode(iinc.var, iinc.incr));
        break;
      case AbstractInsnNode.INSN:
        InsnNode in = (InsnNode) ain;
        list.add(new InsnNode(in.getOpcode()));
        break;
      case AbstractInsnNode.INT_INSN:
        IntInsnNode iin = (IntInsnNode) ain;
        list.add(new IntInsnNode(iin.getOpcode(), iin.operand));
        break;
      case AbstractInsnNode.JUMP_INSN:
        JumpInsnNode jin = (JumpInsnNode) ain;
        list.add(new JumpInsnNode(jin.getOpcode(), cloned.get(jin.label)));
        break;
      case AbstractInsnNode.LABEL:
        if(ain == end) {
          list.add(cloned.get((LabelNode)ain));
          list.add(new InsnNode(Opcodes.RETURN));
          return list;
        }
        list.add(cloned.get((LabelNode)ain));
        break;
      case AbstractInsnNode.LDC_INSN:
        LdcInsnNode ldc = (LdcInsnNode) ain;
        list.add(new LdcInsnNode(ldc.cst));
        break;
      case AbstractInsnNode.LINE:
        LineNumberNode line = (LineNumberNode) ain;
        list.add(new LineNumberNode(line.line, cloned.get(line.start)));
        break;
      case AbstractInsnNode.LOOKUPSWITCH_INSN:
        LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) ain;
        list.add(new LookupSwitchInsnNode(cloned.get(lsin.dflt), toIntArray(lsin.keys),
            mapLabels(lsin.labels, cloned).toArray(new LabelNode[0])));
        break;
      case AbstractInsnNode.METHOD_INSN:
        MethodInsnNode min = (MethodInsnNode) ain;
        list.add(new MethodInsnNode(min.getOpcode(), min.owner, min.name, min.desc));
        break;
      case AbstractInsnNode.MULTIANEWARRAY_INSN:
        MultiANewArrayInsnNode manain = (MultiANewArrayInsnNode) ain;
        list.add(new MultiANewArrayInsnNode(manain.desc, manain.dims));
        break;
      case AbstractInsnNode.TABLESWITCH_INSN:
        TableSwitchInsnNode tsin = (TableSwitchInsnNode) ain;
        list.add(new TableSwitchInsnNode(tsin.min, tsin.max, cloned.get(tsin.dflt),
            mapLabels(tsin.labels, cloned).toArray(new LabelNode[0])));
        break;
      case AbstractInsnNode.TYPE_INSN:
        TypeInsnNode tin = (TypeInsnNode) ain;
        list.add(new TypeInsnNode(tin.getOpcode(), tin.desc));
        break;
      case AbstractInsnNode.VAR_INSN:
        VarInsnNode vin = (VarInsnNode) ain;
        list.add(new VarInsnNode(vin.getOpcode(), vin.var));
        break;
      }
    }
    return list;
  }

  private static int[] toIntArray(List<Integer> list) {
    int[] ret = new int[list.size()];
    int i = 0;
    for (Integer e : list)
      ret[i++] = e.intValue();
    return ret;
  }

  private static ArrayList<LabelNode> mapLabels(List<LabelNode> labels, HashMap<LabelNode, LabelNode> cloned) {
    ArrayList<LabelNode> mapped = new ArrayList<>();
    for (LabelNode l : labels) {
      mapped.add(cloned.get(l));
    }
    return mapped;
  }

  private static HashMap<LabelNode, LabelNode> cloneLabels(InsnList il) {
    HashMap<LabelNode, LabelNode> hm = new HashMap<>();
    for (AbstractInsnNode ain : il.toArray()) {
      if (ain instanceof LabelNode) {
        hm.put((LabelNode) ain, new LabelNode());
      }
    }
    return hm;
  }
}
