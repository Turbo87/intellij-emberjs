
intellij-emberjs
===============================================================================

[![TravisCI](https://img.shields.io/travis/Turbo87/intellij-emberjs/master.svg?label=TravisCI)](https://travis-ci.org/Turbo87/intellij-emberjs/)
[![JetBrains Plugin Repository](https://img.shields.io/github/tag/turbo87/intellij-emberjs.svg?label=JetBrains)](https://plugins.jetbrains.com/plugin/8049)

This plugin provides basic [Ember.js](http://emberjs.com/) support to all
[JetBrains](https://www.jetbrains.com/) IDEs that support JavaScript.


Features
-------------------------------------------------------------------------------

- Ember.js project discovery (via <code>app/app.js</code>) when imported from
  existing sources
- Automatically sets the language level to ES6
- Marks app, public and tests folders as special folders
- Quick navigation via `Navigate → Class...` for all major app components

  ![Navigate → Class...](doc/goto-class.png)


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
