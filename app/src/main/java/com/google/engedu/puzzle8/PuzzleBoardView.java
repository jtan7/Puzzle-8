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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import static java.util.Collections.reverse;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap) {
        int width = getWidth();
        puzzleBoard = new PuzzleBoard(imageBitmap, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        if (puzzleBoard != null) {
            for (int i = 0; i < NUM_SHUFFLE_STEPS; i++) {
                ArrayList<PuzzleBoard> neighbors = puzzleBoard.neighbours();
                puzzleBoard = neighbors.get(random.nextInt(neighbors.size()));
            }
            animation = null;
            puzzleBoard.reset();
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    static class PuzzleBoardComparator implements Comparator<PuzzleBoard>
    {
        @Override
        public int compare(PuzzleBoard x, PuzzleBoard y)
        {
            if (x.priority() < y.priority())
            {
                return -1;
            }
            if (x.priority() > y.priority())
            {
                return 1;
            }
            return 0;
        }
    }

    public void solve() {
        if (puzzleBoard != null) {
            Comparator<PuzzleBoard> comparator = new PuzzleBoardComparator();
            PriorityQueue<PuzzleBoard> puzzleBoardQueue = new PriorityQueue<>(11, comparator);

            puzzleBoard.reset();
            puzzleBoardQueue.add(puzzleBoard);

            while (!puzzleBoardQueue.isEmpty()) {
                PuzzleBoard currBoard = puzzleBoardQueue.poll();
                PuzzleBoard prevBoard = currBoard.getPreviousBoard();

                if (currBoard.resolved()) {
                    ArrayList<PuzzleBoard> solution = new ArrayList<>();
                    solution.add(currBoard);

                    while(prevBoard != null) {
                        solution.add(prevBoard);
                        currBoard = prevBoard;
                        prevBoard = currBoard.getPreviousBoard();
                    }

                    reverse(solution);
                    animation = solution;
                    invalidate();
                    break;
                } else {
                    ArrayList<PuzzleBoard> neighborBoards = currBoard.neighbours();
                    for (int i = 0; i < neighborBoards.size(); i++) {
                        PuzzleBoard neighborBoard = neighborBoards.get(i);
                        if (neighborBoard.equals(prevBoard)) {
                            continue;
                        }
                        puzzleBoardQueue.add(neighborBoard);
                    }
                }
            }
        }
    }

}
