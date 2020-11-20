class Component {}

function each([items]: [any[]]) {}

function _let([value]: [any]) {}

function fn([action, params]: [Function, any[]|undefined]) {}

function array(params: any[]): any[] {
  return []
}
function component([compnent, ...params]: [string|Component, any[]], hash: {[x: string]: any}): Component|undefined {
  return
}
function concat(params: string[]): string {
  return ""
}

function _debugger(params: string[]) {}

function eachIn([object]: [Object]) {}

function get([object, path]: [Object, string]): any {}

const helpers = {
  each,
  'let': _let,
  fn,
  component,
  array,
  concat,
  'debugger': _debugger,
  'each-in': eachIn,
  get
}

export default helpers;


