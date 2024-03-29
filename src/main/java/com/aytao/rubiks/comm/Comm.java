package com.aytao.rubiks.comm;

import java.util.ArrayList;

import com.aytao.rubiks.cube.Move;
import com.aytao.rubiks.cube.Sequence;

public class Comm {

  public static class UnbalancedBracketsException extends IllegalArgumentException {
    private String commString;

    public UnbalancedBracketsException(String commString) {
      super();
      this.commString = commString;
    }

    @Override
    public String toString() {
      return "Comm string '" + this.commString + "' has unbalanced brackets";
    }

  }

  private Component root;
  private String originalString;
  private static final char L_BRACKET = '[';
  private static final char R_BRACKET = ']';

  private static final String NESTED_REGEX = "(?s)(.*\\[.*\\].*)|(.*\\(.*\\).*)";
  private static final String DOUBLE_REGEX = "(?s)\\(.*\\)2";

  public Comm(String commStr) {
    this.originalString = commStr;
    this.root = parse(commStr);
  }

  public ArrayList<Move> toSequence() {
    return root.toSequence();
  }

  public String toString() {
    return originalString;
  }

  /*****************************************************************************
   * Parsing
   ****************************************************************************/
  private static boolean balanced(String commStr) {
    int count = 0;

    for (int i = 0; i < commStr.length(); i++) {
      if (commStr.charAt(i) == L_BRACKET) {
        count++;
      } else if (commStr.charAt(i) == R_BRACKET) {
        count--;
      }

      if (count < 0) {
        return false;
      }
    }

    return count == 0;
  }

  private static boolean isSequential(String commStr) {
    return !commStr.matches(NESTED_REGEX);
  }

  private static boolean isDouble(String commStr) {
    return commStr.matches(DOUBLE_REGEX);
  }

  private static boolean containsMultiple(String commStr) {
    if (commStr.charAt(0) != L_BRACKET || commStr.charAt(commStr.length() - 1) != R_BRACKET) {
      return true;
    }

    int count = 0;
    int numSegments = 0;

    for (int i = 0; i < commStr.length(); i++) {
      if (commStr.charAt(i) == L_BRACKET) {
        count++;
      } else if (commStr.charAt(i) == R_BRACKET) {
        count--;

        if (count == 0) {
          numSegments++;
        }
      }
    }
    return numSegments > 1;
  }

  /*
   * Find and returns the first unbracketed appearance of c, or -1 if there is
   * no such appearance. Does not work for bracket characters ('[' or ']')
   */
  private static int findFirstUnbracketed(String commStr, char c) {
    int count = 0;

    for (int i = 0; i < commStr.length(); i++) {
      if (commStr.charAt(i) == L_BRACKET) {
        count++;
      } else if (commStr.charAt(i) == R_BRACKET) {
        count--;
      } else if (count == 0 && commStr.charAt(i) == c) {
        return i;
      }
    }

    return -1;
  }

  private static Component parse(String commStr) {
    if (!balanced(commStr)) {
      throw new UnbalancedBracketsException("String provided has unbalanced brackets");
    }

    commStr = commStr.trim();

    if (isSequential(commStr)) {
      return new SequenceComponent(commStr);
    }

    if (isDouble(commStr)) {
      return new DoubleComponent(parse(commStr.substring(1, commStr.length() - 2)));
    }

    // Allow unbracketed conjugates
    int colonSplit = findFirstUnbracketed(commStr, ':');
    if (colonSplit >= 0) {
      Component setup = parse(commStr.substring(0, colonSplit));
      Component nested = parse(commStr.substring(colonSplit + 1));
      return new ConjugateComponent(setup, nested);
    }

    if (!containsMultiple(commStr)) {
      assert (commStr.charAt(0) == L_BRACKET && commStr.charAt(commStr.length() - 1) == R_BRACKET);
      commStr = commStr.substring(1, commStr.length() - 1);

      int commaSplit = findFirstUnbracketed(commStr, ',');
      if (commaSplit >= 0) {
        Component first = parse(commStr.substring(0, commaSplit));
        Component second = parse(commStr.substring(commaSplit + 1));
        return new CommutatorComponent(first, second);
      }

      colonSplit = findFirstUnbracketed(commStr, ':');
      if (colonSplit >= 0) {
        Component setup = parse(commStr.substring(0, colonSplit));
        Component nested = parse(commStr.substring(colonSplit + 1));
        return new ConjugateComponent(setup, nested);
      }

      String throwString = "Bracketed area \"" + commStr + "\" does not contain ',' or ':'";
      throw new IllegalArgumentException(throwString);
    }

    throw new UnsupportedOperationException(commStr);

  }

  public static void main(String[] args) {
    String[] tests = { "", "[R, L]", "[U' : [S , R' B R]]", "[R' : [U' R' U , M]]", "[L F' L' , S]",
        "M2' : (U M U M')2", };

    for (String test : tests) {
      Comm comm = new Comm(test);
      System.out.println(Sequence.toString(comm.toSequence()));
    }

  }
}
