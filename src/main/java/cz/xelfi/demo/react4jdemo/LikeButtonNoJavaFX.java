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

import cz.xelfi.demo.react4jdemo.api.React;
import cz.xelfi.demo.react4jdemo.api.React.Element;
import cz.xelfi.demo.react4jdemo.api.React.Props;
import static cz.xelfi.demo.react4jdemo.api.React.props;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;

@Model(className = "LikeState", properties = {
    @Property(name = "liked", type = boolean.class)
})
public final class LikeButtonNoJavaFX extends React.Component<LikeState>  {
    public LikeButtonNoJavaFX(Props props) {
        super(props);
        setState(new LikeState(false));
    }

    void doLike() {
        setState(new LikeState(true));
    }

    @Model(className = "ButtonState", instance = true, properties = {})
    static class ButtonStateCntrl  {
        private LikeButtonNoJavaFX ui;

        @ModelOperation
        void connect(ButtonState model, LikeButtonNoJavaFX ui) {
            this.ui = ui;
        }

        @Function
        void onClick() {
            ui.doLike();
        }
    }

    @Override
    protected Element render() {
        if (this.state().isLiked()) {
            return React.createElement("div", null,
                React.createText("You like React for Java! Continue to the "),
                React.createElement("a", props("href", "ttt1.html"), React.createText("tutorial")),
                React.createText("...")
            );
        }
        final ButtonState buttonState = new ButtonState();
        buttonState.connect(this);

        return React.createElement("button", buttonState, React.createText("Like"));
    }

    private static AutoCloseable root;
    public static void onPageLoad() {
        React.register("LikeButton", LikeButtonNoJavaFX::new);
        root = React.render("LikeButton", "like_button_container");
    }
}
