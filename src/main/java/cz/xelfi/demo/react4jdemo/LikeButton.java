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
import net.java.html.react.React.Props;
import net.java.html.react.RegisterComponent;
import net.java.html.react.Render;

@RegisterComponent(name = "ReactLikeButton")
public abstract class LikeButton extends React.Component<LikeButton.LikeState>  {
    public LikeButton(Props props) {
        super(props);
        setState(new LikeState(false));
    }

    @FXBeanInfo.Generate
    static final class LikeState extends LikeStateBeanInfo {
        final boolean liked;

        LikeState(boolean liked) {
            this.liked = liked;
        }
    }


    void doLike() {
        setState(new LikeState(true));
    }

    @Render(
        "<div>\n" +
        "  You like React for JavaFX Light! See <a href='like.html'>more</a>...\n" +
        "</div>"
    )
    protected abstract Element renderReply();

    @Render(
        "<button onClick='{onClick}'>Like</button>"
    )
    protected abstract Element renderLikeButton(Runnable onClick);

    @Override
    protected Element render() {
        if (this.state().liked) {
            return renderReply();
        } else {
            return renderLikeButton(this::doLike);
        }
    }

    public static void onPageLoad() {
        React.register("LikeButton", ReactLikeButton::new);
        React.render("LikeButton", "like_button_container");
    }
}
