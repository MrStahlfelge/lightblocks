# Falling Lightblocks

![Logo](ios/data/Media.xcassets/Logo.imageset/libgdx@1x.png)

Falling block game for Android (Mobile and TV), iOS, Web browsers. Works on desktops, too.

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/B0B51Z9YB)

## Try it

Falling Lightblocks is available on itch, GameJolt and the usual mobile app stores.
[Visit its website](https://www.golfgl.de/lightblocks/) for the links.

## Build it

![Compile desktop, android and gwt](https://github.com/MrStahlfelge/lightblocks/workflows/Compile%20desktop,%20android%20and%20gwt/badge.svg?branch=master&event=push)

The game is implemented with [libGDX](https://github.com/libgdx/libgdx). Follow the docs to get it
to work.

## License

* The assets in android/assets/ are licensed for your personal use only. You may not redistribute them.
* Source code files and everything else in this repo is licensed under Apache License 2.0

The intention behind this is to:

* allow building own versions of this game for your personal use, to tweak it and try things out, and
to contribute to the project
* allow extending the game and release an own increment of this game with another look and feel
* allow using parts of this project's source code for your own projects of any kind
* prevent indistinguishable clones of this game

## Contribute

Contributions are welcome: bug fixes, enhancements, translations, opening issues etc.

### Translations

There's a single [resource bundle file](android/assets/i18n/strings.properties) with
all strings to be localized. Use it as a template for a new file strings_XX.properties with XX
being the ISO language code.
Please be aware that Falling Lightblocks can (so far) only display Latin based characters.

### Enhancements

Please be aware that
Falling Lightblocks is designed with certain design principles in mind, so if you plan to make some
more efforts, please open an issue to check if your ideas will fit the game.

Some principles the game and its evolvement is based on:
* Features must work on every targeted platform (HTML!) and device
* Features must work with every supported input device (touch, controller, keyboard)
* Changes must not affect scoring of existing game modes
* Falling Lightblocks means classic gameplay, though it is okay if some game modes are guideline

For everyone viewing the source code: Sorry for all the German comments and commit messages that will
make it more difficult for you. I didn't plan to open the source from the beginning, and I tend to
write notes for myself in my native tongue.

## Server
To set up a multiplayer server with a vserver, you can deploy to Heroku or dokku with

     git subtree push --prefix server remotename master

For local play or other setups, you can build a jar file with

     gradlew server:build

OpenJDK8 for building and using recommended.