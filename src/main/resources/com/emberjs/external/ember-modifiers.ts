
type OnArgs = {
  positional: [event: string, fn: Function]
}
function on(_, __, args: OnArgs): any {}

const modifiers = {
  on
}

export default modifiers;


