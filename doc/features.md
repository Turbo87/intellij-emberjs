
Features
===============================================================================


Project Discovery
-------------------------------------------------------------------------------

Ember.js projects are automatically discovered when imported via 
`File → New → Project from Existing Sources...`. The importer will look for an
`app/app.js` file and will flag the project as an Ember.js project if the file
exists.

The language level for Ember.js projects is automatically set to ES6 and
JSHint support is enabled using the `.jshintrc` file.


Marking special folders
-------------------------------------------------------------------------------

The importer will mark certain folders like this:

- `app` as `Sources Root`
- `public` as `Resources Root`
- `tests`, `tests/unit` and `tests/integration` as `Tests Root`
- `dist` and `tmp` as `Excluded`

![Project View](project-view.png)

This will cause the "Packages" view to look like this:

![Packages View](packages-view.png)


Quick Navigation
-------------------------------------------------------------------------------

### `Navigate → Class...`

The plugin is indexing all typical components in the `app` folder and is
providing them for quick access via the `Navigate → Class...` action. Currently
supported are:

- Adapters 
- Components
- Controllers
- Helpers
- Models
- Routes
- Serializers
- Services
- Transforms

The indexer will transform file paths into class names by capitalizing the
path parts and appending a type suffix

Example: `app/routes/pets/index.js` will be indexed as `PetsIndexRoute`

![Navigate → Class...](goto-class.png)


### `Navigate → Related Symbol...`

The plugin provides an implementation for the `Navigate → Related Symbol...`
quick navigation.

Example: Invoking the action inside of `/app/routes/crate/index.js` will
switch to the `/app/controllers/crate/index.js` file if it exists.
 
The following groups of files can be cycled like this:
 
- `controllers`, `routes`, `templates`
- `components`, `templates`
- `adapters`, `models`, `serializers`

The same functionality can also be used to cycle between tests and the tested
components (i.e. `/app/routes/crate/index.js` and 
`/tests/unit/routes/crate/index.js`)


### Navigating via references

Some Ember.js method calls result in container lookups and can not be resolved
statically. The Ember.js plugin implements a best effort reference resolver
for cases like `DS.belongsTo('user')`, where <kbd>ctrl</kbd>-clicking the
`'user'` literal will navigate directly to the `user` model if it exists.

Have a look at the 
[`EmberReferenceContributor`](../src/main/kotlin/com/emberjs/psi/EmberReferenceContributor.kt) 
class to check for which methods this functionality is available.


Using `ember-cli` from the IDE
-------------------------------------------------------------------------------

Some of the `ember-cli` commands are exposed right in the IDE for quick access.

### Generating files

The Ember.js plugin can be used to quickly generate files using the `generate`
command of `ember-cli`. This functionality is available as 
`File → New → Ember.js Code`.

![Blueprints Dialog](blueprints-dialog.png)


Live Templates
-------------------------------------------------------------------------------

Live templates are snippets with variables that are automatically expanded by
the editor. For example: if you enter `comp` and press the <kbd>Tab</kbd> key
it will be expanded into a computed property:

![Live Templates](live-templates.png)

The following live templates are available:

- `alias` – computed property alias
- `comp` – computed property
- `compset` – computed property with setter
- `compx` – computed property method

-------------------------------------------------------------------------------

Most screenshots in this document are taking from an import of the 
[crates.io](https://github.com/rust-lang/crates.io) project.
