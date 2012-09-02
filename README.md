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

clojure-noob is built using the [Gradle][1] build tool, which
I happen to think is really sweet.  To see a list of build
tasks, run:

    $ ./gradlew tasks

The build will automatically download any dependencies it
needs, including Gradle itself!  The only thing you need to
have pre-installed is a Java 5+ JDK.

### Build Artifacts

**TODO**


[1]: http://www.gradle.org/     "Gradle"
