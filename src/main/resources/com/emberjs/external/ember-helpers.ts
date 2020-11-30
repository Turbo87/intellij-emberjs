class Component {}

/**
 The `{{#each}}` helper loops over elements in a collection. It is an extension
 of the base Handlebars `{{#each}}` helper.
 The default behavior of `{{#each}}` is to yield its inner block once for every
 item in an array passing the item as the first block parameter.
 Assuming the `@developers` argument contains this array:
 ```javascript
 [{ name: 'Yehuda' },{ name: 'Tom' }, { name: 'Paul' }];
 ```
 ```handlebars
 <ul>
 {{#each @developers as |person|}}
 <li>Hello, {{person.name}}!</li>
 {{/each}}
 </ul>
 ```
 The same rules apply to arrays of primitives.
 ```javascript
 ['Yehuda', 'Tom', 'Paul']
 ```
 ```handlebars
 <ul>
 {{#each @developerNames as |name|}}
 <li>Hello, {{name}}!</li>
 {{/each}}
 </ul>
 ```
 During iteration, the index of each item in the array is provided as a second block
 parameter.
 ```handlebars
 <ul>
 {{#each @developers as |person index|}}
 <li>Hello, {{person.name}}! You're number {{index}} in line</li>
 {{/each}}
 </ul>
 ```
 ### Specifying Keys
 In order to improve rendering speed, Ember will try to reuse the DOM elements
 where possible. Specifically, if the same item is present in the array both
 before and after the change, its DOM output will be reused.
 The `key` option is used to tell Ember how to determine if the items in the
 array being iterated over with `{{#each}}` has changed between renders. By
 default the item's object identity is used.
 This is usually sufficient, so in most cases, the `key` option is simply not
 needed. However, in some rare cases, the objects' identities may change even
 though they represent the same underlying data.
 For example:
 ```javascript
 people.map(person => {
    return { ...person, type: 'developer' };
  });
 ```
 In this case, each time the `people` array is `map`-ed over, it will produce
 an new array with completely different objects between renders. In these cases,
 you can help Ember determine how these objects related to each other with the
 `key` option:
 ```handlebars
 <ul>
 {{#each @developers key="name" as |person|}}
 <li>Hello, {{person.name}}!</li>
 {{/each}}
 </ul>
 ```
 By doing so, Ember will use the value of the property specified (`person.name`
 in the example) to find a "match" from the previous render. That is, if Ember
 has previously seen an object from the `@developers` array with a matching
 name, its DOM elements will be re-used.
 There are two special values for `key`:
 * `@index` - The index of the item in the array.
 * `@identity` - The item in the array itself.
 ### {{else}} condition
 `{{#each}}` can have a matching `{{else}}`. The contents of this block will render
 if the collection is empty.
 ```handlebars
 <ul>
 {{#each @developers as |person|}}
 <li>{{person.name}} is available!</li>
 {{else}}
 <li>Sorry, nobody is available for this task.</li>
 {{/each}}
 </ul>
 ```
 @method each
 @for Ember.Templates.helpers
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/each?anchor=each
 @public
 */
function each([items]: [any[]]) {}

/**
 The `let` helper receives one or more positional arguments and yields
 them out as block params.
 This allows the developer to introduce shorter names for certain computations
 in the template.
 This is especially useful if you are passing properties to a component
 that receives a lot of options and you want to clean up the invocation.
 For the following example, the template receives a `post` object with
 `content` and `title` properties.
 We are going to call the `my-post` component, passing a title which is
 the title of the post suffixed with the name of the blog, the content
 of the post, and a series of options defined in-place.
 ```handlebars
 {{#let
        (concat post.title ' | The Ember.js Blog')
        post.content
        (hash
          theme="high-contrast"
          enableComments=true
        )
        as |title content options|
    }}
 <MyPost @title={{title}} @content={{content}} @options={{options}} />
 {{/let}}
 ```
 or
 ```handlebars
 {{#let
        (concat post.title ' | The Ember.js Blog')
        post.content
        (hash
          theme="high-contrast"
          enableComments=true
        )
        as |title content options|
    }}
 {{my-post title=title content=content options=options}}
 {{/let}}
 ```
 @method let
 @for Ember.Templates.helpers
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/let?anchor=let
 @public
 */
function _let([value]: [any]) {}

/**
 The `fn` helper allows you to ensure a function that you are passing off
 to another component, helper, or modifier has access to arguments that are
 available in the template.
 For example, if you have an `each` helper looping over a number of items, you
 may need to pass a function that expects to receive the item as an argument
 to a component invoked within the loop. Here's how you could use the `fn`
 helper to pass both the function and its arguments together:
 ```app/templates/components/items-listing.hbs
 {{#each @items as |item|}}
 <DisplayItem @item=item @select={{fn this.handleSelected item}} />
 {{/each}}
 ```
 ```app/components/items-list.js
 import Component from '@glimmer/component';
 import { action } from '@ember/object';
 export default class ItemsList extends Component {
    @action
    handleSelected(item) {
      // ...snip...
    }
  }
 ```
 In this case the `display-item` component will receive a normal function
 that it can invoke. When it invokes the function, the `handleSelected`
 function will receive the `item` and any arguments passed, thanks to the
 `fn` helper.
 Let's take look at what that means in a couple circumstances:
 - When invoked as `this.args.select()` the `handleSelected` function will
 receive the `item` from the loop as its first and only argument.
 - When invoked as `this.args.select('foo')` the `handleSelected` function
 will receive the `item` from the loop as its first argument and the
 string `'foo'` as its second argument.
 In the example above, we used `@action` to ensure that `handleSelected` is
 properly bound to the `items-list`, but let's explore what happens if we
 left out `@action`:
 ```app/components/items-list.js
 import Component from '@glimmer/component';
 export default class ItemsList extends Component {
    handleSelected(item) {
      // ...snip...
    }
  }
 ```
 In this example, when `handleSelected` is invoked inside the `display-item`
 component, it will **not** have access to the component instance. In other
 words, it will have no `this` context, so please make sure your functions
 are bound (via `@action` or other means) before passing into `fn`!
 See also [partial application](https://en.wikipedia.org/wiki/Partial_application).
 @method fn
 @for Ember.Templates.helpers
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/fn?anchor=fn
 @public
 @since 3.11.0
 */
function fn(params: [action: Function, ...args: any[]]) {}

/**
 Use the `{{array}}` helper to create an array to pass as an option to your
 components.
 ```handlebars
 <MyComponent @people={{array
     'Tom Dale'
     'Yehuda Katz'
     this.myOtherPerson}}
 />
 ```
 or
 ```handlebars
 {{my-component people=(array
     'Tom Dale'
     'Yehuda Katz'
     this.myOtherPerson)
   }}
 ```
 Would result in an object such as:
 ```js
 ['Tom Dale', 'Yehuda Katz', this.get('myOtherPerson')]
 ```
 Where the 3rd item in the array is bound to updates of the `myOtherPerson` property.
 @method array
 @for Ember.Templates.helpers
 @param {Array} options
 @return {Array} Array
 @since 3.8.0
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/array?anchor=array
 @public
 */
function array(options: any[]): any[] {
  return []
}

/**
 The `{{component}}` helper lets you add instances of `Component` to a
 template. See [Component](/ember/release/classes/Component) for
 additional information on how a `Component` functions.
 `{{component}}`'s primary use is for cases where you want to dynamically
 change which type of component is rendered as the state of your application
 changes. This helper has three modes: inline, block, and nested.
 ### Inline Form
 Given the following template:
 ```app/application.hbs
 {{component this.infographicComponentName}}
 ```
 And the following application code:
 ```app/controllers/application.js
 import Controller from '@ember/controller';
 import { tracked } from '@glimmer/tracking';
 export default class ApplicationController extends Controller {
    @tracked isMarketOpen = 'live-updating-chart'
    get infographicComponentName() {
      return this.isMarketOpen ? 'live-updating-chart' : 'market-close-summary';
    }
  }
 ```
 The `live-updating-chart` component will be appended when `isMarketOpen` is
 `true`, and the `market-close-summary` component will be appended when
 `isMarketOpen` is `false`. If the value changes while the app is running,
 the component will be automatically swapped out accordingly.
 Note: You should not use this helper when you are consistently rendering the same
 component. In that case, use standard component syntax, for example:
 ```app/templates/application.hbs
 <LiveUpdatingChart />
 ```
 or
 ```app/templates/application.hbs
 {{live-updating-chart}}
 ```
 ### Block Form
 Using the block form of this helper is similar to using the block form
 of a component. Given the following application template:
 ```app/templates/application.hbs
 {{#component this.infographicComponentName}}
 Last update: {{this.lastUpdateTimestamp}}
 {{/component}}
 ```
 The following controller code:
 ```app/controllers/application.js
 import Controller from '@ember/controller';
 import { computed } from '@ember/object';
 import { tracked } from '@glimmer/tracking';
 export default class ApplicationController extends Controller {
    @tracked isMarketOpen = 'live-updating-chart'
    get lastUpdateTimestamp() {
      return new Date();
    }
    get infographicComponentName() {
      return this.isMarketOpen ? 'live-updating-chart' : 'market-close-summary';
    }
  }
 ```
 And the following component template:
 ```app/templates/components/live-updating-chart.hbs
 {{! chart }}
 {{yield}}
 ```
 The `Last Update: {{this.lastUpdateTimestamp}}` will be rendered in place of the `{{yield}}`.
 ### Nested Usage
 The `component` helper can be used to package a component path with initial attrs.
 The included attrs can then be merged during the final invocation.
 For example, given a `person-form` component with the following template:
 ```app/templates/components/person-form.hbs
 {{yield (hash
    nameInput=(component "my-input-component" value=@model.name placeholder="First Name")
  )}}
 ```
 When yielding the component via the `hash` helper, the component is invoked directly.
 See the following snippet:
 ```
 <PersonForm as |form|>
 <form.nameInput @placeholder="Username" />
 </PersonForm>
 ```
 or
 ```
 {{#person-form as |form|}}
 {{form.nameInput placeholder="Username"}}
 {{/person-form}}
 ```
 Which outputs an input whose value is already bound to `model.name` and `placeholder`
 is "Username".
 When yielding the component without the `hash` helper use the `component` helper.
 For example, below is a `full-name` component template:
 ```handlebars
 {{yield (component "my-input-component" value=@model.name placeholder="Name")}}
 ```
 ```
 <FullName as |field|>
 {{component field placeholder="Full name"}}
 </FullName>
 ```
 or
 ```
 {{#full-name as |field|}}
 {{component field placeholder="Full name"}}
 {{/full-name}}
 ```
 @method component
 @since 1.11.0
 @for Ember.Templates.helpers
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/component?anchor=component
 @public
 */
function component([compnent, ...params]: [string|Component, any[]], hash: {[x: string]: any}): Component|undefined {
  return
}

/**
 Concatenates the given arguments into a string.
 Example:
 ```handlebars
 {{some-component name=(concat firstName " " lastName)}}
 {{! would pass name="<first name value> <last name value>" to the component}}
 ```
 or for angle bracket invocation, you actually don't need concat at all.
 ```handlebars
 <SomeComponent @name="{{firstName}} {{lastName}}" />
 ```
 @public
 @method concat
 @for Ember.Templates.helpers
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/concat?anchor=concat
 @since 1.13.0
 */
function concat(params: string[]): string {
  return ""
}


/**
 Execute the `debugger` statement in the current template's context.
 ```handlebars
 {{debugger}}
 ```
 When using the debugger helper you will have access to a `get` function. This
 function retrieves values available in the context of the template.
 For example, if you're wondering why a value `{{foo}}` isn't rendering as
 expected within a template, you could place a `{{debugger}}` statement and,
 when the `debugger;` breakpoint is hit, you can attempt to retrieve this value:
 ```
 > get('foo')
 ```
 `get` is also aware of keywords. So in this situation
 ```handlebars
 {{#each this.items as |item|}}
 {{debugger}}
 {{/each}}
 ```
 You'll be able to get values from the current item:
 ```
 > get('item.name')
 ```
 You can also access the context of the view to make sure it is the object that
 you expect:
 ```
 > context
 ```
 @method debugger
 @for Ember.Templates.helpers
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/debugger?anchor=debugger
 @public
 */
function _debugger(params: string[]) {}

/**
 The `{{each-in}}` helper loops over properties on an object.
 For example, given this component definition:
 ```app/components/developer-details.js
 import Component from '@glimmer/component';
 import { tracked } from '@glimmer/tracking';
 export default class extends Component {
    @tracked developer = {
      "name": "Shelly Sails",
      "age": 42
    };
  }
 ```
 This template would display all properties on the `developer`
 object in a list:
 ```app/components/developer-details.hbs
 <ul>
 {{#each-in this.developer as |key value|}}
 <li>{{key}}: {{value}}</li>
 {{/each-in}}
 </ul>
 ```
 Outputting their name and age.
 @method each-in
 @for Ember.Templates.helpers
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/each-in?anchor=each-in
 @public
 @since 2.1.0
 */
function eachIn([object]: [Object]) {}

/**
 Dynamically look up a property on an object. The second argument to `{{get}}`
 should have a string value, although it can be bound.
 For example, these two usages are equivalent:
 ```app/components/developer-detail.js
 import Component from '@glimmer/component';
 import { tracked } from '@glimmer/tracking';
 export default class extends Component {
    @tracked developer = {
      name: "Sandi Metz",
      language: "Ruby"
    }
  }
 ```
 ```handlebars
 {{this.developer.name}}
 {{get this.developer "name"}}
 ```
 If there were several facts about a person, the `{{get}}` helper can dynamically
 pick one:
 ```app/templates/application.hbs
 <DeveloperDetail @factName="language" />
 ```
 ```handlebars
 {{get this.developer @factName}}
 ```
 For a more complex example, this template would allow the user to switch
 between showing the user's height and weight with a click:
 ```app/components/developer-detail.js
 import Component from '@glimmer/component';
 import { tracked } from '@glimmer/tracking';
 export default class extends Component {
    @tracked developer = {
      name: "Sandi Metz",
      language: "Ruby"
    }
    @tracked currentFact = 'name'
    @action
    showFact(fact) {
      this.currentFact = fact;
    }
  }
 ```
 ```app/components/developer-detail.js
 {{get this.developer this.currentFact}}
 <button {{on 'click' (fn this.showFact "name")}}>Show name</button>
 <button {{on 'click' (fn this.showFact "language")}}>Show language</button>
 ```
 The `{{get}}` helper can also respect mutable values itself. For example:
 ```app/components/developer-detail.js
 <Input @value={{mut (get this.person this.currentFact)}} />
 <button {{on 'click' (fn this.showFact "name")}}>Show name</button>
 <button {{on 'click' (fn this.showFact "language")}}>Show language</button>
 ```
 Would allow the user to swap what fact is being displayed, and also edit
 that fact via a two-way mutable binding.
 @public
 @method get
 @for Ember.Templates.helpers
 @since 2.1.0
 */
function get([object, path]: [Object, string]): any {}

/**
 The `if` helper allows you to conditionally render one of two branches,
 depending on the "truthiness" of a property.
 For example the following values are all falsey: `false`, `undefined`, `null`, `""`, `0`, `NaN` or an empty array.
 This helper has two forms, block and inline.
 ## Block form
 You can use the block form of `if` to conditionally render a section of the template.
 To use it, pass the conditional value to the `if` helper,
 using the block form to wrap the section of template you want to conditionally render.
 Like so:
 ```app/templates/application.hbs
 <Weather />
 ```
 ```app/components/weather.hbs
 {{! will not render because greeting is undefined}}
 {{#if @isRaining}}
 Yes, grab an umbrella!
 {{/if}}
 ```
 You can also define what to show if the property is falsey by using
 the `else` helper.
 ```app/components/weather.hbs
 {{#if @isRaining}}
 Yes, grab an umbrella!
 {{else}}
 No, it's lovely outside!
 {{/if}}
 ```
 You are also able to combine `else` and `if` helpers to create more complex
 conditional logic.
 For the following template:
 ```app/components/weather.hbs
 {{#if @isRaining}}
 Yes, grab an umbrella!
 {{else if @isCold}}
 Grab a coat, it's chilly!
 {{else}}
 No, it's lovely outside!
 {{/if}}
 ```
 If you call it by saying `isCold` is true:
 ```app/templates/application.hbs
 <Weather @isCold={{true}} />
 ```
 Then `Grab a coat, it's chilly!` will be rendered.
 ## Inline form
 The inline `if` helper conditionally renders a single property or string.
 In this form, the `if` helper receives three arguments, the conditional value,
 the value to render when truthy, and the value to render when falsey.
 For example, if `useLongGreeting` is truthy, the following:
 ```app/templates/application.hbs
 <Greeting @useLongGreeting={{true}} />
 ```
 ```app/components/greeting.hbs
 {{if @useLongGreeting "Hello" "Hi"}} Alex
 ```
 Will render:
 ```html
 Hello Alex
 ```
 One detail to keep in mind is that both branches of the `if` helper will be evaluated,
 so if you have `{{if condition "foo" (expensive-operation "bar")`,
  `expensive-operation` will always calculate.
  @method if
  @for Ember.Templates.helpers
  @see https://api.emberjs.com/ember/release/classes/Ember.Templates.helpers/methods/if?anchor=if
  @public
 */
function _if(params: [condition: boolean]): any {}

const helpers = {
  each,
  'let': _let,
  fn,
  component,
  array,
  concat,
  'debugger': _debugger,
  'each-in': eachIn,
  get,
  'if': _if
}

export default helpers;


