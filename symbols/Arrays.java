package symbols;

import lexer.Tag;

public class Arrays extends Type {
  public Type of; // arranjo *of* type
  public int size = 1; // número de elementos

 public Arrays(int sz, Type p) {
  super("[]", Tag.INDEX, sz*p.width);
  size = sz;
  of = p;
  }

  public String toString() {
    return "Array(" + size + ", " + of.toString() + ")";
  }

  public int __len__() {
    return size;
  }

  public Type __getitem__(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("index " + index + " out of range for array of size " + size);
    }
    return of;
  }
public boolean __setitem__(int index, Type valueType) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("index " + index + " out of range for array of size " + size);
    }
    if (valueType == null) return false;
    return valueType.equals(of);
  }
}