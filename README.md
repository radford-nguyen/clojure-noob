clojure-noob
============

I am a Clojure noob specifically, and a functional programming
noob in general.  I'm using this project not just to learn
Clojure but also to train myself in how to think functionally.
I have absolutely no doubt that the code in this project is
inefficient, inelegant, and just downright wrong in many ways.
May the functional gods have mercy on my soul.

clojure-noob is a random text generator that works by
building a Markov Chain from some seed data.  I don't take
any pains to make it particularly good at generating very
"random" or "believable" text, as I am more concerned with
the programming journey, so to speak.

Building clojure-noob
---------------------

clojure-noob can be built with either [Leiningen][1] or [Gradle][2].

    $ ./lein uberjar
    $ ./gradlew uberjar

The build will automatically download any dependencies it needs,
including Leiningen/Gradle itself.  The only thing you need to
have pre-installed is a Java 5+ JDK.

Once the build has completed, you will find a fat jar in the
build folder (different for each build tool.)

[1]: http://leiningen.org/     "Leiningen"
[2]: http://gradle.org/     "Gradle"


## License

Copyright © 2014 radford-nguyen

Distributed under the Eclipse Public License, the same as Clojure.
