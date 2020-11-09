class Component<T> {}

type LinkToArgs = {
  route: string
  disabled?: boolean
  activeClass?: string
  'current-when'?: string
  model?: any
}

/**
 The `LinkTo` component renders a link to the supplied `routeName` passing an optionally
 supplied model to the route as its `model` context of the route. The block for `LinkTo`
 becomes the contents of the rendered element:
 ```handlebars
 <LinkTo @route='photoGallery'>
 Great Hamster Photos
 </LinkTo>
 ```
 This will result in:
 ```html
 <a href="/hamster-photos">
 Great Hamster Photos
 </a>
 ```
 ### Disabling the `LinkTo` component
 The `LinkTo` component can be disabled by using the `disabled` argument. A disabled link
 doesn't result in a transition when activated, and adds the `disabled` class to the `<a>`
 element.
 (The class name to apply to the element can be overridden by using the `disabledClass`
 argument)
 ```handlebars
 <LinkTo @route='photoGallery' @disabled={{true}}>
 Great Hamster Photos
 </LinkTo>
 ```
 ### Handling `href`
 `<LinkTo>` will use your application's Router to fill the element's `href` property with a URL
 that matches the path to the supplied `routeName`.
 ### Handling current route
 The `LinkTo` component will apply a CSS class name of 'active' when the application's current
 route matches the supplied routeName. For example, if the application's current route is
 'photoGallery.recent', then the following invocation of `LinkTo`:
 ```handlebars
 <LinkTo @route='photoGallery.recent'>
 Great Hamster Photos
 </LinkTo>
 ```
 will result in
 ```html
 <a href="/hamster-photos/this-week" class="active">
 Great Hamster Photos
 </a>
 ```
 The CSS class used for active classes can be customized by passing an `activeClass` argument:
 ```handlebars
 <LinkTo @route='photoGallery.recent' @activeClass="current-url">
 Great Hamster Photos
 </LinkTo>
 ```
 ```html
 <a href="/hamster-photos/this-week" class="current-url">
 Great Hamster Photos
 </a>
 ```
 ### Keeping a link active for other routes
 If you need a link to be 'active' even when it doesn't match the current route, you can use the
 `current-when` argument.
 ```handlebars
 <LinkTo @route='photoGallery' @current-when='photos'>
 Photo Gallery
 </LinkTo>
 ```
 This may be helpful for keeping links active for:
 * non-nested routes that are logically related
 * some secondary menu approaches
 * 'top navigation' with 'sub navigation' scenarios
 A link will be active if `current-when` is `true` or the current
 route is the route this link would transition to.
 To match multiple routes 'space-separate' the routes:
 ```handlebars
 <LinkTo @route='gallery' @current-when='photos drawings paintings'>
 Art Gallery
 </LinkTo>
 ```
 ### Supplying a model
 An optional `model` argument can be used for routes whose
 paths contain dynamic segments. This argument will become
 the model context of the linked route:
 ```javascript
 Router.map(function() {
    this.route("photoGallery", {path: "hamster-photos/:photo_id"});
  });
 ```
 ```handlebars
 <LinkTo @route='photoGallery' @model={{this.aPhoto}}>
 {{aPhoto.title}}
 </LinkTo>
 ```
 ```html
 <a href="/hamster-photos/42">
 Tomster
 </a>
 ```
 ### Supplying multiple models
 For deep-linking to route paths that contain multiple
 dynamic segments, the `models` argument can be used.
 As the router transitions through the route path, each
 supplied model argument will become the context for the
 route with the dynamic segments:
 ```javascript
 Router.map(function() {
    this.route("photoGallery", { path: "hamster-photos/:photo_id" }, function() {
      this.route("comment", {path: "comments/:comment_id"});
    });
  });
 ```
 This argument will become the model context of the linked route:
 ```handlebars
 <LinkTo @route='photoGallery.comment' @models={{array this.aPhoto this.comment}}>
 {{comment.body}}
 </LinkTo>
 ```
 ```html
 <a href="/hamster-photos/42/comments/718">
 A+++ would snuggle again.
 </a>
 ```
 ### Supplying an explicit dynamic segment value
 If you don't have a model object available to pass to `LinkTo`,
 an optional string or integer argument can be passed for routes whose
 paths contain dynamic segments. This argument will become the value
 of the dynamic segment:
 ```javascript
 Router.map(function() {
    this.route("photoGallery", { path: "hamster-photos/:photo_id" });
  });
 ```
 ```handlebars
 <LinkTo @route='photoGallery' @model={{aPhotoId}}>
 {{this.aPhoto.title}}
 </LinkTo>
 ```
 ```html
 <a href="/hamster-photos/42">
 Tomster
 </a>
 ```
 When transitioning into the linked route, the `model` hook will
 be triggered with parameters including this passed identifier.
 ### Allowing Default Action
 By default the `<LinkTo>` component prevents the default browser action by calling
 `preventDefault()` to avoid reloading the browser page.
 If you need to trigger a full browser reload pass `@preventDefault={{false}}`:
 ```handlebars
 <LinkTo @route='photoGallery' @model={{this.aPhotoId}} @preventDefault={{false}}>
 {{this.aPhotoId.title}}
 </LinkTo>
 ```
 ### Supplying a `tagName`
 By default `<LinkTo>` renders an `<a>` element. This can be overridden for a single use of
 `<LinkTo>` by supplying a `tagName` argument:
 ```handlebars
 <LinkTo @route='photoGallery' @tagName='li'>
 Great Hamster Photos
 </LinkTo>
 ```
 This produces:
 ```html
 <li>
 Great Hamster Photos
 </li>
 ```
 In general, this is not recommended. Instead, you can use the `transition-to` helper together
 with a click event handler on the HTML tag of your choosing.
 ### Supplying query parameters
 If you need to add optional key-value pairs that appear to the right of the ? in a URL,
 you can use the `query` argument.
 ```handlebars
 <LinkTo @route='photoGallery' @query={{hash page=1 per_page=20}}>
 Great Hamster Photos
 </LinkTo>
 ```
 This will result in:
 ```html
 <a href="/hamster-photos?page=1&per_page=20">
 Great Hamster Photos
 </a>
 ```
 @for Ember.Templates.components
 @method LinkTo
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.components/methods/input?anchor=LinkTo
 @public
 */

class LinkToComponent extends Component<LinkToArgs> {

}

/**
 The `Input` component lets you create an HTML `<input>` element.
 ```handlebars
 <Input @value="987" />
 ```
 creates an `<input>` element with `type="text"` and value set to 987.
 ### Text field
 If no `type` argument is specified, a default of type 'text' is used.
 ```handlebars
 Search:
 <Input @value={{this.searchWord}} />
 ```
 In this example, the initial value in the `<input>` will be set to the value of
 `this.searchWord`. If the user changes the text, the value of `this.searchWord` will also be
 updated.
 ### Actions
 The `Input` component takes a number of arguments with callbacks that are invoked in response to
 user events.
 * `enter`
 * `insert-newline`
 * `escape-press`
 * `focus-in`
 * `focus-out`
 * `key-down`
 * `key-press`
 * `key-up`
 These callbacks are passed to `Input` like this:
 ```handlebars
 <Input @value={{this.searchWord}} @enter={{this.query}} />
 ```
 ### `<input>` HTML Attributes to Avoid
 In most cases, if you want to pass an attribute to the underlying HTML `<input>` element, you
 can pass the attribute directly, just like any other Ember component.
 ```handlebars
 <Input @type="text" size="10" />
 ```
 In this example, the `size` attribute will be applied to the underlying `<input>` element in the
 outputted HTML.
 However, there are a few attributes where you **must** use the `@` version.
 * `@type`: This argument is used to control which Ember component is used under the hood
 * `@value`: The `@value` argument installs a two-way binding onto the element. If you wanted a
 one-way binding, use `<input>` with the `value` property and the `input` event instead.
 * `@checked` (for checkboxes): like `@value`, the `@checked` argument installs a two-way binding
 onto the element. If you wanted a one-way binding, use `<input type="checkbox">` with
 `checked` and the `input` event instead.
 ### Extending `TextField`
 Internally, `<Input @type="text" />` creates an instance of `TextField`, passing arguments from
 the helper to `TextField`'s `create` method. Subclassing `TextField` is supported but not
 recommended.
 See [TextField](/ember/release/classes/TextField)
 ### Checkbox
 To create an `<input type="checkbox">`:
 ```handlebars
 Emberize Everything:
 <Input @type="checkbox" @checked={{this.isEmberized}} name="isEmberized" />
 ```
 This will bind the checked state of this checkbox to the value of `isEmberized` -- if either one
 changes, it will be reflected in the other.
 ### Extending `Checkbox`
 Internally, `<Input @type="checkbox" />` creates an instance of `Checkbox`. Subclassing
 `TextField` is supported but not recommended.
 See [Checkbox](/ember/release/classes/Checkbox)
 @method Input
 @for Ember.Templates.components
 @see {TextField}
 @see {Checkbox}
 @see https://api.emberjs.com/ember/release/classes/Ember.Templates.components/methods/Input?anchor=Input
 @param {Hash} options
 @public
 */
class InputComponent extends Component<{
  /**
   *  The @value argument installs a two-way binding onto the element. If you wanted a one-way binding, use <input> with the value property and the input event instead.
   */
  value?: any
  /**
   *  (for checkboxes): like @value, the @checked argument installs a two-way binding onto the element. If you wanted a one-way binding, use <input type="checkbox"> with checked and the input event instead.
   */
  checked?: boolean
  /**
   *  This argument is used to control which Ember component is used under the hood
   */
  type?: 'checkbox|button|color|date|datetime-local|email|file|hidden|image|month|number|password|radio|range|reset|search|submit|tel|text|time|url|week'
  'enter'?: Function,
  'insert-newline'?: Function,
  'escape-press'?: Function,
  'focus-in'?: Function,
  'focus-out'?: Function,
  'key-press'?: Function,
  'key-down'?: Function,
  'key-up'?: Function
}> {

}

/**
 The `Textarea` component inserts a new instance of `<textarea>` tag into the template.
 The `@value` argument provides the content of the `<textarea>`.
 This template:
 ```handlebars
 <Textarea @value="A bunch of text" />
 ```
 Would result in the following HTML:
 ```html
 <textarea class="ember-text-area">
 A bunch of text
 </textarea>
 ```
 The `@value` argument is two-way bound. If the user types text into the textarea, the `@value`
 argument is updated. If the `@value` argument is updated, the text in the textarea is updated.
 In the following example, the `writtenWords` property on the component will be updated as the user
 types 'Lots of text' into the text area of their browser's window.
 ```app/components/word-editor.js
 import Component from '@glimmer/component';
 import { tracked } from '@glimmer/tracking';
 export default class WordEditorComponent extends Component {
    @tracked writtenWords = "Lots of text that IS bound";
  }
 ```
 ```handlebars
 <Textarea @value={{writtenWords}} />
 ```
 Would result in the following HTML:
 ```html
 <textarea class="ember-text-area">
 Lots of text that IS bound
 </textarea>
 ```
 If you wanted a one way binding, you could use the `<textarea>` element directly, and use the
 `value` DOM property and the `input` event.
 ### Actions
 The `Textarea` component takes a number of arguments with callbacks that are invoked in
 response to user events.
 * `enter`
 * `insert-newline`
 * `escape-press`
 * `focus-in`
 * `focus-out`
 * `key-press`
 These callbacks are passed to `Textarea` like this:
 ```handlebars
 <Textarea @value={{this.searchWord}} @enter={{this.query}} />
 ```
 ## Classic Invocation Syntax
 The `Textarea` component can also be invoked using curly braces, just like any other Ember
 component.
 For example, this is an invocation using angle-bracket notation:
 ```handlebars
 <Textarea @value={{this.searchWord}} @enter={{this.query}} />
 ```
 You could accomplish the same thing using classic invocation:
 ```handlebars
 {{textarea value=this.searchWord enter=this.query}}
 ```
 The main difference is that angle-bracket invocation supports any HTML attribute using HTML
 attribute syntax, because attributes and arguments have different syntax when using angle-bracket
 invocation. Curly brace invocation, on the other hand, only has a single syntax for arguments,
 and components must manually map attributes onto component arguments.
 When using classic invocation with `{{textarea}}`, only the following attributes are mapped onto
 arguments:
 * rows
 * cols
 * name
 * selectionEnd
 * selectionStart
 * autocomplete
 * wrap
 * lang
 * dir
 * value
 ## Classic `layout` and `layoutName` properties
 Because HTML `textarea` elements do not contain inner HTML the `layout` and
 `layoutName` properties will not be applied.
 @method Textarea
 @for Ember.Templates.components
 @see {TextArea}
 @see https://api.emberjs.com/ember/3.22/classes/Ember.Templates.components/methods/Textarea?anchor=Textarea
 @public
 */
class TextareaComponent extends Component<{
  rows?: number,
  cols?: number,
  name?: string,
  selectionEnd?: number,
  selectionStart?: number
  /**
   * This attribute indicates whether the value of the control can be automatically completed by the browser. Possible values are:
   off: The user must explicitly enter a value into this field for every use, or the document provides its own auto-completion method; the browser does not automatically complete the entry.
   on: The browser can automatically complete the value based on values that the user has entered during previous uses.
   If the autocomplete attribute is not specified on a <textarea> element, then the browser uses the autocomplete attribute value of the <textarea> element's form owner. The form owner is either the <form> element that this <textarea> element is a descendant of or the form element whose id is specified by the form attribute of the input element. For more information, see the autocomplete attribute in <form>.
   @see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/textarea#attr-autocomplete
   */
  autocomplete?: boolean
  wrap?: boolean
  lang?: string
  dir?: string
  value?: any
  'enter'?: Function,
  'insert-newline'?: Function,
  'escape-press'?: Function,
  'focus-in'?: Function,
  'focus-out'?: Function,
  'key-press'?: Function,
}> {

}

const components = {
   LinkTo: LinkToComponent,
  Textarea: TextareaComponent,
   Input: InputComponent
}

export default components;


