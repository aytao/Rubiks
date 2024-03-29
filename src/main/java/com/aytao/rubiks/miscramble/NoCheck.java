package com.aytao.rubiks.miscramble;

import com.aytao.rubiks.cube.Cube;

public class NoCheck extends ScrambleChecker {

  @Override
  public boolean checkMisscramble(Cube misscrambledCube, Cube correctlyScrambledCube) {
    return true;
  }

  @Override
  public String toString() {
    return "No Check: " + getMissRate();
  }
}
