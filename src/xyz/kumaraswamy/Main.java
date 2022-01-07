package xyz.kumaraswamy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import xyz.kumaraswamy.lzw.LZW;

public class Main {

  public static void main(String[] args) throws IOException {
    byte[] texts =
        Files.readAllBytes(
            new File("C:\\Users\\user\\Documents\\LZW Bytes\\files\\text.txt")
                .toPath()
        );
    Byte[] bytes = new Byte[texts.length];
    Arrays.parallelSetAll(bytes, i -> texts[i]);

    byte[] encode = new LZW(bytes).encode();

    System.out.println("Saved Bytes: " + (texts.length - encode.length));
    System.out.println("Encoded: " + Arrays.toString(encode));

    byte[] decode = new LZW().decode(encode);
    System.out.println("decoded: " + Arrays.toString(decode));

    for (int i = 0; i < texts.length; i++) {
      if (texts[i] != decode[i]) {
        System.out.println("Not correct");
        break;
      }
    }
    System.out.println("Yay");
    Files.write(
        new File("C:\\Users\\user\\Documents\\LZW Bytes\\files\\decoded.txt")
            .toPath()
        , texts);
  }
}
