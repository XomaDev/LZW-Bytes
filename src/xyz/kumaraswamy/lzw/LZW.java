package xyz.kumaraswamy.lzw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class LZW {

  private static final byte max_delim_use = 2;
  private byte delim = 0;

  private static final int mode_normal = 0;
  private static final byte mode_store = 7;

  private static final int range = 2;

  private final Byte[] bytes;

  private Iterator<Byte> indexByteI;

  private HashMap<ArrayList<Byte>, Byte> dict;
  private int mode = mode_normal;

  public LZW() {
    this(null);
  }

  public LZW(Byte[] bytes) {
    this.bytes = bytes;
    if (bytes == null) {
      return;
    }
    generateIndexes();
  }

  private void generateIndexes() {
    ArrayList<Byte> bytesI = new ArrayList<>();
    loop:
    for (int i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
      if (i == delim) {
        continue;
      }
      for (byte byt : bytes) {
        if (byt == i) {
          continue loop;
        }
      }
      bytesI.add((byte) i);
    }
    this.indexByteI = bytesI.iterator();
    if (!indexByteI.hasNext()) {
      mode = mode_store;
      return;
    }
    delim = indexByteI.next();
  }

  public byte[] encode() throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    if (mode == mode_store) {
      // don't write anything
      bytes.write(mode);
      for (byte byt : this.bytes) {
        bytes.write(byt);
      }
      return bytes.toByteArray();
    }
    bytes.write(_encode().toByteArray());
    bytes.write(delim);
    bytes.write(mode);
    bytes.write(LZW.range);
    bytes.write(delim);
    dict.forEach((byts, rep) -> {
      bytes.write(rep);
      byts.forEach(bytes::write);
    });
    return bytes.toByteArray();
  }

  private ByteArrayOutputStream _encode() {
    ArrayList<Byte> bytesI = new ArrayList<>(List.of(bytes.clone()));

    dict = new HashMap<>();
    Range range = new Range(LZW.range, false);

    for (int i = 0; i < bytesI.size(); i++) {
      byte byt = bytesI.get(i);
      range.put(byt);
      if (range.hasAll() && contains(range.array(), bytesI)) {
        ArrayList<Byte> cloned = new ArrayList<>(bytesI);

        while (true) {
          int index =
              Collections.indexOfSubList(
                  cloned, range.array()
              );
          if (index == -1) {
            break;
          }
          List<Byte> bytes = cloned.subList(
              index,
              index + range.array().size());
          bytes.clear();

          Byte by = dict.get(range.array());
          if (by == null) {
            by = indexByteI.next();
            dict.put(range.array(), by);
          }
          bytes.add(by);
        }
        bytesI = cloned;
      }
    }
    final ArrayList<Byte> bytesIF = bytesI;
    return new ByteArrayOutputStream(bytes.length) {{
      write(mode);
      if (mode != mode_store) {
        write(delim);
        bytesIF.forEach(this::write);
      }
    }};
  }

  public byte[] decode(byte[] bytes) {
    int timesNull = 0;

    ByteArrayOutputStream encoded = new ByteArrayOutputStream(),
        dict = new ByteArrayOutputStream();

    byte mode = bytes[0];
    if (mode == mode_store) {
      return Arrays.copyOfRange(bytes, 1, bytes.length);
    }
    byte delim = bytes[1];
    int range = -1;

    for (int i = 2; i < bytes.length; i++) {
      byte byt = bytes[i];
      if (byt == delim) {
        if (timesNull == max_delim_use) {
          dict.write(byt);
        } else {
          timesNull++;
        }
      } else if (timesNull == 0) {
        encoded.write(byt);
      } else if (timesNull == 1) {
        range = byt;
      } else if (timesNull == 2) {
        dict.write(byt);
      }
    }
    byte[] bytes_dict = dict.toByteArray();
    if (bytes_dict.length % (range + 1) != 0) {
      throw new IllegalArgumentException();
    }

    Byte key = null;

    Range bytesI = new Range(range, false);

    HashMap<Byte, ArrayList<Byte>> dictI = new HashMap<>();

    for (byte byt : bytes_dict) {
      if (key == null) {
        key = byt;
        continue;
      }
      bytesI.put(byt);
      if (bytesI.hasAll()) {
        dictI.put(key, bytesI.array());
        bytesI = new Range(range, false);
        key = null;
      }
    }
    return decodeByDict(encoded.toByteArray(), dictI);
  }

  private byte[] decodeByDict(byte[] bytes, HashMap<Byte, ArrayList<Byte>> dict) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    for (byte byt : bytes) {
      ArrayList<Byte> byts = dict.get(byt);
      if (byts == null) {
        stream.write(byt);
      } else {
        byte[] bytesI = new byte[byts.size()];
        for (int i = 0; i < byts.size(); i++) {
          bytesI[i] = byts.get(i);
        }
        for (byte by : decodeByDict(bytesI, dict)) {
          stream.write(by);
        }
      }
    }
    return stream.toByteArray();
  }

  private boolean contains(List<Byte> sublist, ArrayList<Byte> bytesI) {
    bytesI = new ArrayList<>(bytesI);
    int t = 0;
    while (true) {
      int index =
          Collections.indexOfSubList(
              bytesI, sublist
          );
      if (index == -1) {
        break;
      }
      if (++t == 2) {
        return true;
      }
      int to = index + sublist.size();
      bytesI.subList(index, to).clear();
    }
    return false;
  }
}
