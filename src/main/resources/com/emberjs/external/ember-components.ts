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

class InputComponent extends Component<any> {

    }

class TextareaComponent extends Component<any> {

    }

const components = {
    LinkTo: LinkToComponent,
    Textarea: TextareaComponent,
    Input: InputComponent
}

export default components;


