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
import cz.xelfi.demo.react4jdemo.api.React;
import cz.xelfi.demo.react4jdemo.api.React.Element;
import static cz.xelfi.demo.react4jdemo.api.React.props;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicTacToe3 {

    private static Object cSquare;
    private static Object cBoard;
    private static Object cGame;

    private TicTacToe3() {
    }

    static class Square extends React.Component<Void> {
        final String className = "square";

        Square(React.Props props) {
            super(props);
        }

        @Override
        protected React.Element render() {
            String text = (String) getProperty("value");
            return React.createElement("button", JsUtils.onButton("square", () -> {
                onEvent("onClick");
            }), React.createText(text));
        }
    }

    static class Board extends React.Component<List<Character>> {

        Board(React.Props props) {
            super(props);
            final List<Character> nine = Collections.nCopies(9, null);
            setState(nine);
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
                onClick.run();
            }
        }

        private void handleClick(int i) {
            List<Character> arr = new ArrayList<>(state());
            arr.set(i, 'X');
            setState(arr);
        }

        private Element renderSquare(int i) {
            final Character ith = state().get(i);
            System.err.println("it: " + ith);
            final Runnable ithClick = () -> { handleClick(i); };
            System.err.println("ithClick: " + ithClick);
            return React.createElement(cSquare, new SquareProps(ith == null ? null : "" + ith, ithClick));
        }

        @Override
        protected Element render() {
            final Element status = React.createText("Next player: X");

            return React.createElement("div", null,
                    React.createElement("div", props("className", "status"), status),
                    React.createElement("div", props("className", "board-row"),
                            renderSquare(0),
                            renderSquare(1),
                            renderSquare(2)
                    ),
                    React.createElement("div", props("className", "board-row"),
                            renderSquare(3),
                            renderSquare(4),
                            renderSquare(5)
                    ),
                    React.createElement("div", props("className", "board-row"),
                            renderSquare(6),
                            renderSquare(7),
                            renderSquare(8)
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
        cSquare = React.register("Square", Square::new);
        cBoard = React.register("Board", Board::new);
        cGame = React.register("Game", Game::new);
        React.render("Game", "root");
    }

}
