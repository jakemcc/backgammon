# backgammon

Multiplayer backgammon game that uses Firebase for shared state.

This project was used as the example for a talk I gave at IN/Clojure 2018 on titled "Serverless Applications using ClojureScript and Firebase."

# To run:

1. Sign up for [firebase](firebase.com)
1. Make a project through the console.
1. Grab the configuration from the Firebase console and shove it [here](https://github.com/jakemcc/backgammon/blob/master/src/cljs/bg/firebase.cljs#L9-L14). (Pass it as an argument to the `firebase.initializeApp` function).
1. Modify `.firebaserc` to use your project's id.
1. `lein cljsbuild once min`
1. `firebase deploy` (Local project won't work till this happens either because this will change some authentication settings for your firebase realtime database)
1. `lein figwheel dev` for local development.

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build and Deploy


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
firebase deploy
```
