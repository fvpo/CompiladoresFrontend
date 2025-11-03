package main;

import inter.Node;
import java.lang.reflect.*;
import java.util.*;

public class ASTPrinter {
    private static final int MAX_DEPTH = 50;

    public static void print(Node root) {
        printNode(root, 0, new HashSet<>());
    }

    private static void printNode(Object obj, int indent, Set<Object> seen) {
        if (obj == null) {
            printIndent(indent);
            System.out.println("null");
            return;
        }

        if (indent > MAX_DEPTH) {
            printIndent(indent);
            System.out.println("... (max depth)");
            return;
        }

        // Prevent cycles (by identity)
        if (!(obj instanceof Number) && !(obj instanceof String) && !(obj instanceof Character) && !(obj instanceof Boolean)) {
            if (seen.contains(obj)) {
                printIndent(indent);
                System.out.println("(circular ref) " + obj.getClass().getSimpleName());
                return;
            }
            seen.add(obj);
        }

        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            printIndent(indent);
            System.out.println("List(size=" + list.size() + ")");
            for (Object e : list) {
                printNode(e, indent + 2, seen);
            }
            return;
        }

        if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            printIndent(indent);
            System.out.println("Array(size=" + len + ")");
            for (int i = 0; i < len; i++) {
                printNode(Array.get(obj, i), indent + 2, seen);
            }
            return;
        }

        if (obj instanceof String || obj instanceof Number || obj instanceof Character || obj instanceof Boolean) {
            printIndent(indent);
            System.out.println(obj.toString());
            return;
        }

        // If it's an AST node or other object, print class and fields
        Class<?> cls = obj.getClass();
        printIndent(indent);
        System.out.println(cls.getSimpleName());

        // Reflect fields
        Field[] fields = cls.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (Field f : fields) {
            // skip synthetic / transient / static
            int mods = f.getModifiers();
            if (Modifier.isStatic(mods)) continue;
            if (f.isSynthetic()) continue;

            Object val;
            try {
                val = f.get(obj);
            } catch (IllegalAccessException e) {
                continue;
            }

            printIndent(indent + 2);
            System.out.print(f.getName() + ": ");
            if (val == null) {
                System.out.println("null");
            } else if (val instanceof String || val instanceof Number || val instanceof Character || val instanceof Boolean) {
                System.out.println(val.toString());
            } else if (val instanceof List || val.getClass().isArray()) {
                System.out.println();
                printNode(val, indent + 4, seen);
            } else if (val.getClass().getPackage() != null && val.getClass().getPackage().getName().startsWith("inter")) {
                System.out.println();
                printNode(val, indent + 4, seen);
            } else {
                // print simple toString for other types
                System.out.println(String.valueOf(val));
            }
        }
    }

    private static void printIndent(int indent) {
        for (int i = 0; i < indent; i++) System.out.print(' ');
    }
}
