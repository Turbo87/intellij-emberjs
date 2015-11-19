
intellij-emberjs
===============================================================================

[![TravisCI](https://img.shields.io/travis/Turbo87/intellij-emberjs/master.svg?label=TravisCI)](https://travis-ci.org/Turbo87/intellij-emberjs/)

This plugin provides basic [Ember.js](http://emberjs.com/) support to all
[JetBrains](https://www.jetbrains.com/) IDEs that support JavaScript.


Features
-------------------------------------------------------------------------------

- Ember.js project discovery when imported from existing sources
- Automatically sets the language level to ES6
- Marks app, public and tests folders as source, resource and test folders
- Marks node_modules and bower_components as library folders
- Enable JSHint using `.jshintrc`
- Quick navigation via `Navigate → Class...` for all major app components

[more...](doc/features.md)


Installation
-------------------------------------------------------------------------------

This plugin is published on the
[JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/8049): 

    Preferences... → Plugins → Browse Repositories ... → Search for "Ember.js"


### From Source

Clone this repository:

    git clone https://github.com/Turbo87/intellij-emberjs.git
    cd intellij-emberjs

Build a plugin zip file:

    ./gradlew buildPlugin

Install the plugin from `/build/distributions/Ember.js.zip`:

    Preferences... → Plugins → Install plugin from disk ...


License
-------------------------------------------------------------------------------

This project is licensed under the [Apache 2.0 License](LICENSE).

- [Font-Awesome-SVG-PNG](https://github.com/encharm/Font-Awesome-SVG-PNG) is licensed under the MIT license
- [Font-Awesome](http://fontawesome.io/) is licensed under the [SIL OFL 1.1](http://scripts.sil.org/OFL)
