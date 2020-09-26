# React for Java Demo

Visit [online version](http://xelfi.cz/react4jdemo/) to get a taste for what 
this demo offers - e.g. using __React.js__ and controlling it from __Java__.

# Try it yourself!

Clone the repository and invoke `gradlew` to play with the demo yourself:

```bash
$ git clone --single-branch --branch react4jdemo https://github.com/jtulach/netbeans-html4j react4jdemo
$ cd react4jdemo
react4jdemo$ JAVA_HOME=/jdk-11 ./gradlew bck2brwsrShow
```

Modify the HTML files in `src/main/webapp/pages/` and the Java files
in `src/main/java/` and repeat `./gradlew bck2brwsrShow` to see the changes.

# Debugging

It is possible to debug your application in JDK 11 HotSpot mode instead of running
it in a browser. Rather than using `bck2brwsrShow` use:

```bash
react4jdemo$ JAVA_HOME=/jdk-11 ./gradlew run --debug-jvm --args index.html
```

and attach your IDE to port `5005`. Then you can debug sources like
`src/main/java/cz/xelfi/demo/react4jdemo/LikeButton.java`, place breakpoints
into `doLike` methods, etc.

You can run the application without `--args index.html` - it is the default. 
But you can also use any other page listed in `src/main/webapp/pages/`. For 
example:

```bash
react4jdemo$ JAVA_HOME=/jdk-11 ./gradlew run --args ttt1.html
```

Launches the 
[TicTacToe1.java](https://github.com/jtulach/netbeans-html4j/blob/react4jdemo/src/main/java/cz/xelfi/demo/react4jdemo/TicTacToe1.java)
component. Other possible arguments include
`ttt2.html`, `ttt3.html` - see the `switch` in 
[OnPageLoad](https://github.com/jtulach/netbeans-html4j/blob/react4jdemo/src/main/java/cz/xelfi/demo/react4jdemo/OnPageLoad.java)
for a full list.