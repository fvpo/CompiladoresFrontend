package symbols;
import lexer.Tag;

public class StringType extends Type{
  public int sizeOf; // número de elementos
    public StringType(int length) {
        super("string", Tag.BASIC, 16*length); // assumindo tamanho 16 para referência
        sizeOf = length;
    }

  public int __len__() {
    return sizeOf;
  }

  public Type __getitem__(Integer start, Integer stop) {
    int s = (start == null) ? 0 : start;
    int e = (stop == null) ? sizeOf : stop;
    if (s < 0) s += sizeOf;
    if (e < 0) e += sizeOf;
    s = Math.max(0, Math.min(s, sizeOf));
    e = Math.max(0, Math.min(e, sizeOf));
    int len = Math.max(0, e - s);
    return new StringType(len);
  }

  public boolean __setitem__(int index, Type valueType) {
    throw new UnsupportedOperationException("strings are immutable; item assignment not supported");
  }
  
  public String __repr__() {
    return "string(" + sizeOf + ")";
  }

}
