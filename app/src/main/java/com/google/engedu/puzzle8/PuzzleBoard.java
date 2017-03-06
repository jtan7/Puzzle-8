/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;


public class PuzzleBoard {

    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles;
    private int steps;
    private PuzzleBoard previousBoard;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        steps = 0;
        int size = bitmap.getWidth();
        if (size > bitmap.getHeight()) {
            size = bitmap.getHeight();
        }
        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, 0, 0, size, size);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(squareBitmap, parentWidth, parentWidth, false);
        int width = parentWidth / NUM_TILES;
        tiles = new ArrayList<>();
        for (int r = 0; r < NUM_TILES; r++) {
            for (int c = 0; c < NUM_TILES; c++) {
                Bitmap chunk = Bitmap.createBitmap(scaledBitmap, c * width, r * width, width, width);
                PuzzleTile tile = new PuzzleTile(chunk, r * NUM_TILES + c);
                tiles.add(tile);
            }
        }
        if (tiles.size() > 0) {
            tiles.remove(NUM_TILES * NUM_TILES - 1);
            tiles.add(null);
        }
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        steps = otherBoard.steps += 1;
        previousBoard = otherBoard;
    }

    public void reset() {
        steps = 0;
        previousBoard = null;
    }

    public PuzzleBoard getPreviousBoard() {
        return previousBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        // locate the empty square in the current board
        boolean foundEmptyTile = false;

        int emptyTile = tiles.indexOf(null);
        int r = emptyTile / NUM_TILES;
        int c = emptyTile % NUM_TILES;

        ArrayList<PuzzleBoard> results = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (withinBounds(r, c, NEIGHBOUR_COORDS[i])) {
                int neighborTile = (r + NEIGHBOUR_COORDS[i][0]) * NUM_TILES + (c + NEIGHBOUR_COORDS[i][1]);
                swapTiles(emptyTile, neighborTile);
                PuzzleBoard newBoard = new PuzzleBoard(this);
                results.add(newBoard);
                swapTiles(neighborTile, emptyTile);
            }
        }

        return results;
    }

    private boolean withinBounds(int r, int c, int[] neighbor) {
        if (r + neighbor[0] >= 0 && r + neighbor[0] < NUM_TILES &&
                c + neighbor[1] >= 0 && c + neighbor[1] < NUM_TILES) {
            return true;
        }
        return false;
    }

    public int priority() {
        int manhattanDistance = 0;
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            int currR = i / NUM_TILES;
            int currC = i % NUM_TILES;

            int tileNum;
            if (tiles.get(i) == null) {
                tileNum = NUM_TILES * NUM_TILES - 1;
            } else {
                tileNum = tiles.get(i).getNumber();
            }
            int goalR = tileNum / NUM_TILES;
            int goalC = tileNum % NUM_TILES;

            manhattanDistance += Math.abs(currR - goalR) + Math.abs(currC - goalC);
        }
        return manhattanDistance + steps;
    }

}
