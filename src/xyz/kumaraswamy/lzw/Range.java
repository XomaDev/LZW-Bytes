package xyz.kumaraswamy.lzw;

import java.util.ArrayList;
import java.util.Objects;

public class Range {

  private final int r;
  private final boolean debug;

  private final ArrayList<Byte> array;

  private int len = 0;

  public Range(int r, boolean debug) {
    this.r = r;
    this.debug = debug;

    array = new ArrayList<>(r);
  }

  public void put(Byte b) {
    if (b == null) {
      return;
    }
    if (len == r) {
      for (int i = 0; i < r; i++) {
        Byte next = i + 1 >= r ? null : array.get(i + 1);
        array.set(i, next);
      }
      array.set(r - 1, b);
      return;
    }
    array.add(b);
    len++;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<Byte> array() {
    return (ArrayList<Byte>) array.clone();
  }

  public boolean hasAll() {
    if (debug) {
      System.out.println("Has All Called: " + array);
    }
    return r == len;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Range range = (Range) o;
    ArrayList<Byte> bytes = range.array;
    for (int i = 0; i < bytes.size(); i++) {
      if (!Objects.equals(bytes.get(i), array.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "Range{" +
        "array=" + array +
        '}';
  }
}
