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

import net.java.html.react.React;
import net.java.html.react.React.Element;
import static net.java.html.react.React.props;
import net.java.html.react.RegisterComponent;
import net.java.html.react.Render;

public class TicTacToe2 {

    private static Object cSquare;
    private static Object cBoard;
    private static Object cGame;

    private TicTacToe2() {
    }

    @RegisterComponent(name = "TicTacToe2Square")
    static abstract class Square extends React.Component<Character> {
        final String className = "square";

        Square(React.Props props) {
            super(props);
            setState(null);
        }

        @Render(
            "<button className='square' onClick='{click}'>{text}</button>"
        )
        protected abstract React.Element renderSquare(String text, Runnable click);

        @Override
        protected React.Element render() {
            return renderSquare(state() == null ? null : state().toString(), () -> {
                setState('X');
            });
        }
    }

    static class Board extends React.Component {

        Board(React.Props props) {
            super(props);
        }

        private Element renderSquare(int i) {
            return React.createElement(cSquare, props("value", "" + i));
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
        cSquare = React.register("Square", TicTacToe2Square::new);
        cBoard = React.register("Board", Board::new);
        cGame = React.register("Game", Game::new);
        React.render("Game", "root");
    }

}
