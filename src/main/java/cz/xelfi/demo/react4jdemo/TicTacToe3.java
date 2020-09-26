/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cz.xelfi.demo.react4jdemo;

import com.dukescript.api.javafx.beans.FXBeanInfo;
import net.java.html.react.React;
import net.java.html.react.React.Element;
import static net.java.html.react.React.props;
import net.java.html.react.RegisterComponent;
import net.java.html.react.Render;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicTacToe3 {

    private static Object cSquare;
    private static Object cBoard;
    private static Object cGame;

    private TicTacToe3() {
    }

    @RegisterComponent(name = "TicTacToe3Square")
    static abstract class Square extends React.Component<Void> {
        final String className = "square";

        Square(React.Props props) {
            super(props);
        }

        @Render(
            "<button className='square' onClick='{click}'>{text}</button>"
        )
        protected abstract React.Element renderButton(String text, Runnable click);

        @Override
        protected React.Element render() {
            String text = (String) getProperty("value");
            return renderButton(text, () -> {
                onEvent("onClick");
            });
        }
    }

    @FXBeanInfo.Generate
    static class BoardState extends BoardBase3 {
        final List<Character> squares;
        final boolean xIsNext;

        BoardState(List<Character> squares, boolean xIsNext) {
            this.squares = squares;
            this.xIsNext = xIsNext;
        }
    }

    static class Board extends React.Component<BoardState> {

        Board(React.Props props) {
            super(props);
            final List<Character> nine = Collections.nCopies(9, null);
            setState(new BoardState(nine, true));
        }

        @FXBeanInfo.Generate
        static class SquareProps extends SquareBase {
            private final Runnable onClick;
            final String value;

            SquareProps(String value, Runnable onClick) {
                this.value = value;
                this.onClick = onClick;
            }

            final void onClick() {
                JsUtils.debugger();
                if (onClick != null) {
                    onClick.run();
                }
            }
        }

        private void handleClick(int i) {
            List<Character> arr = new ArrayList<>(state().squares);
            if (state().xIsNext) {
                arr.set(i, 'X');
            } else {
                arr.set(i, 'O');
            }
            setState(new BoardState(arr, !state().xIsNext));
        }

        private Element renderSquare(boolean gameOver, int i) {
            final Character ith = state().squares.get(i);
            final Runnable ithClick = gameOver || ith != null ? null : () -> { handleClick(i); };
            return React.createElement(cSquare, new SquareProps(ith == null ? null : "" + ith, ithClick));
        }

        private static final int[][] winningLines = new int[][]{
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8},
            {2, 4, 6},};

        private static Character calculateWinner(List<Character> squares) {
            for (int i = 0; i < winningLines.length; i++) {
                int[] line = winningLines[i];
                Character ch0 = squares.get(line[0]);
                Character ch1 = squares.get(line[1]);
                Character ch2 = squares.get(line[2]);
                if (ch0 != null && ch0.equals(ch1) && ch1.equals(ch2)) {
                    return ch0;
                }
            }
            return null;
        }

        @Override
        protected Element render() {
            final Character winner = calculateWinner(state().squares);
            final boolean gameOver = winner != null;
            final Element status;
            if (winner == null) {
                status = React.createText("Next player: " + (state().xIsNext ? 'X' : 'O'));
            } else {
                status = React.createText("Winner " + winner);
            }

            return React.createElement("div", null,
                    React.createElement("div", props("className", "status"), status),
                    React.createElement("div", props("className", "board-row"),
                            renderSquare(gameOver, 0),
                            renderSquare(gameOver, 1),
                            renderSquare(gameOver, 2)
                    ),
                    React.createElement("div", props("className", "board-row"),
                            renderSquare(gameOver, 3),
                            renderSquare(gameOver, 4),
                            renderSquare(gameOver, 5)
                    ),
                    React.createElement("div", props("className", "board-row"),
                            renderSquare(gameOver, 6),
                            renderSquare(gameOver, 7),
                            renderSquare(gameOver, 8)
                    )
            );
        }
    }

    static class Game extends React.Component {

        public Game(React.Props props) {
            super(props);
        }

        protected Element render() {
            return React.createElement("div", props("className", "game"),
                    React.createElement("div", props("className", "game-board"),
                            React.createElement(cBoard, null)
                    ),
                    React.createElement("div", props("className", "game-info"),
                            React.createElement("div", null),
                            React.createElement("ol", null)
                    )
            );
        }
    }

    public static void onPageLoad() {
        cSquare = React.register("Square", TicTacToe3Square::new);
        cBoard = React.register("Board", Board::new);
        cGame = React.register("Game", Game::new);
        React.render("Game", "root");
    }

}
