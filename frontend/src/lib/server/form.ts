/** Reads a text field from submitted form data, ignoring file uploads. */
export function formString(form: FormData, name: string) {
  const value = form.get(name);
  return typeof value === 'string' ? value : '';
}
