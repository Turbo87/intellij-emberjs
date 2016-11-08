package com.emberjs.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmberCliBlueprintListParserTest {

    @Test fun testParse() {
        val output = """
Requested ember-cli commands:

ember generate <blueprint> <options...>
  Generates new code from blueprints.
  aliases: g
  --dry-run (Boolean) (Default: false)
    aliases: -d
  --verbose (Boolean) (Default: false)
    aliases: -v
  --pod (Boolean) (Default: false)
    aliases: -p
  --classic (Boolean) (Default: false)
    aliases: -c
  --dummy (Boolean) (Default: false)
    aliases: -dum, -id
  --in-repo-addon (String) (Default: null)
    aliases: --in-repo <value>, -ir <value>


  Available blueprints:
    ember-simple-auth:
      authenticator <name> <options...>
        Generates an Ember Simple Auth authenticator
        --base-class=oauth2|devise|torii|base (String) (Default: base)
      authorizer <name> <options...>
        Generates an Ember Simple Auth authorizer
        --base-class=oauth2|devise|base (String) (Default: base)
    ember-power-select:
      ember-power-select <name>
    ember-page-title:
      ember-page-title <name>
    ember-cp-validations:
      ember-cp-validations <name>
      validator <name>
        Generates a validator and unit test
      validator-test <name>
        Generates a validator unit test
    ember-intl:
      ember-intl <name>
        Setup ember-intl
      ember-intl-config <name>
        Create a new boilerplate configuration file
      ember-intl-dot-notation <name>
        Used to support dot notated translation keys
      translation <name>
        Adds an empty translation file and locale is supported
    ember-cli-sentry:
      ember-cli-sentry <name>
      logger <name>
        Generates a Raven Logger service.
    ember-font-awesome:
      ember-font-awesome <name>
    ember-cli-mocha:
      acceptance-test <name>
        Generates an acceptance test for a feature.
      adapter-test <name>
        Generates an ember-data adapter unit test
      component-test <name> <options...>
        Generates a component integration or unit test.
        --test-type (String) (Default: integration)
          aliases: -i (--test-type=integration), -u (--test-type=unit), --integration (--test-type=integration), -unit (--test-type=unit)
      controller-test <name>
        Generates a controller unit test.
      ember-cli-mocha <name>
      helper-test <name>
        Generates a helper unit test.
      initializer-test <name>
        Generates an initializer unit test.
      instance-initializer-test <name>
        Generates an instance initializer unit test.
      mixin-test <name>
        Generates a mixin unit test.
      model-test <name>
        Generates a model unit test.
      route-test <name>
        Generates a route unit test.
      serializer-test <name>
        Generates a serializer unit test.
      service-test <name>
        Generates a service unit test.
      transform-test <name>
        Generates a transform unit test.
      util-test <name>
        Generates a util unit test.
      view-test <name>
        Generates a view unit test.
    ember-cli-eslint:
      ember-cli-eslint <name>
    ember-cli-deploy:
      ember-cli-deploy <name>
        Generate config for ember-cli deployments
    ember-cli-legacy-blueprints:
      adapter <name> <options...>
        Generates an ember-data adapter.
        --base-class (String)
      component <name> <options...>
        Generates a component. Name must contain a hyphen.
        --path (String) (Default: components)
          aliases: --no-path (--path=)
      component-addon <name>
        Generates a component. Name must contain a hyphen.
      controller <name>
        Generates a controller.
      helper <name>
        Generates a helper function.
      helper-addon <name>
        Generates an import wrapper.
      initializer <name>
        Generates an initializer.
      initializer-addon <name>
        Generates an import wrapper.
      instance-initializer <name>
        Generates an instance initializer.
      instance-initializer-addon <name>
        Generates an import wrapper.
      mixin <name>
        Generates a mixin.
      model <name> <attr:type>
        Generates an ember-data model.
      resource <name>
        Generates a model and route.
      route <name> <options...>
        Generates a route and a template, and registers the route with the router.
        --path (String) (Default: )
        --skip-router (Boolean) (Default: false)
        --reset-namespace (Boolean)
      route-addon <name>
        Generates import wrappers for a route and its template.
      serializer <name>
        Generates an ember-data serializer.
      service <name>
        Generates a service.
      template <name>
        Generates a template.
      test-helper <name>
        Generates a test helper.
      transform <name>
        Generates an ember-data value transform.
      util <name>
        Generates a simple utility module/function.
      view <name>
        Generates a view subclass.
    ember-cli:
      addon <name>
        The default blueprint for ember-cli addons.
      addon-import <name>
        Generates an import wrapper.
      app <name>
        The default blueprint for ember-cli projects.
      blueprint <name>
        Generates a blueprint and definition.
      http-mock <endpoint-path>
        Generates a mock api endpoint in /api prefix.
      http-proxy <local-path> <remote-url>
        Generates a relative proxy to another server.
      in-repo-addon <name>
        The blueprint for addon in repo ember-cli addons.
      lib <name>
        Generates a lib directory for in-repo addons.
      server <name>
        Generates a server directory for mocks and proxies.
      vendor-shim <name>
        Generates an ES6 module shim for global libraries.

    """

        val blueprints = EmberCliBlueprintListParser().parse(output)

        val blueprintNames = blueprints.map { it.name }
        assertThat(blueprintNames).contains("component", "controller", "route", "template")
        assertThat(blueprintNames).contains("component-test", "controller-test", "route-test")
        assertThat(blueprintNames).contains("authenticator", "authorizer")

        val routeBlueprint = blueprints.find { it.name == "route" }
        assertThat(routeBlueprint).isNotNull()
        assertThat(routeBlueprint?.description).isEqualTo("Generates a route and a template, and registers the route with the router.")
        assertThat(routeBlueprint?.args).isEqualTo(listOf("--path", "--skip-router", "--reset-namespace"))
    }
}
