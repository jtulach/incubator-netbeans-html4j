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
import net.java.html.react.RegisterComponent;
import net.java.html.react.Render;

public class TicTacToe2 {
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
            return renderSquare(state() == null ? "" : state().toString(), () -> {
                setState('X');
            });
        }
    }

    @RegisterComponent(name = "TicTacToe2Board")
    static abstract class Board extends React.Component {

        Board(React.Props props) {
            super(props);
        }

        @Render(
            "<Square value='{i}'/>"
        )

        protected abstract React.Element renderSquare(int i);

        @Render(
            "<div>" +
            "  <div className='status'>{status}</div>" +
            "  <div className='board-row'>" +
            "    {this.renderSquare(0)}" +
            "    {this.renderSquare(1)}" +
            "    {this.renderSquare(2)}" +
            "  </div>" +
            "  <div className='board-row'>" +
            "    {this.renderSquare(3)}" +
            "    {this.renderSquare(4)}" +
            "    {this.renderSquare(5)}" +
            "  </div>" +
            "  <div className='board-row'>" +
            "    {this.renderSquare(6)}" +
            "    {this.renderSquare(7)}" +
            "    {this.renderSquare(8)}" +
            "  </div>" +
            "</div>"
        )
        protected abstract Element renderBoard(String status);

        @Override
        protected Element render() {
            return renderBoard("Next player: X");
        }
    }

    @RegisterComponent(name = "TicTacToe2Game")
    static abstract class Game extends React.Component {

        public Game(React.Props props) {
            super(props);
        }

        @Render(
            "<div className='game'>" +
            "  <div className='game-board'>" +
            "    <Board/>" +
            "  </div>" +
            "  <div className='game-info'>" +
            "    <div></div>" +
            "    <ol></ol>" +
            "  </div>" +
            "</div>"
        )
        @Override
        protected abstract Element render();

    }

    public static void onPageLoad() {
        React.register("Square", TicTacToe2Square::new);
        React.register("Board", TicTacToe2Board::new);
        React.register("Game", TicTacToe2Game::new);
        React.render("Game", "root");
    }

}
