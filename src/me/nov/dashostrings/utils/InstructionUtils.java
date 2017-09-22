package me.nov.dashostrings.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class InstructionUtils implements Opcodes {

	public static boolean isNumber(AbstractInsnNode ain) {
		if (ain.getOpcode() == BIPUSH || ain.getOpcode() == SIPUSH) {
			return true;
		}
		if (ain.getOpcode() >= ICONST_M1 && ain.getOpcode() <= ICONST_5) {
			return true;
		}
		if (ain instanceof LdcInsnNode) {
			LdcInsnNode ldc = (LdcInsnNode) ain;
			if (ldc.cst instanceof Number) {
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
		if(node instanceof LdcInsnNode) {
			LdcInsnNode ldc = (LdcInsnNode) node;
			return Integer.parseInt(ldc.cst.toString());
		}
		return 0;
	}

	public static String getStringValue(AbstractInsnNode node) {
		if (node.getType() == AbstractInsnNode.LDC_INSN) {
			LdcInsnNode ldc = (LdcInsnNode) node;
			return ldc.cst.toString();
		}
		return "";
	}
}
