import { describe, expect, it } from 'vitest';

import { pointerPath, problemFields, problemMessage } from './problem';

describe('problem details', () => {
  it('reads the detail, else the title', () => {
    expect(problemMessage({ title: 'Not Found', status: 404, detail: 'User 9 not found' })).toBe('User 9 not found');
    expect(problemMessage({ title: 'Not Found', status: 404 })).toBe('Not Found');
    expect(problemMessage('<html>Bad Gateway</html>')).toBeUndefined();
  });

  it('turns field pointers into paths', () => {
    expect(pointerPath('#/items/1/name')).toEqual(['items', '1', 'name']);
    expect(pointerPath('#/a~1b/c~0d')).toEqual(['a/b', 'c~d']);
    expect(problemFields({ errors: [{ pointer: '#/username', detail: 'too short' }, { pointer: 1 }, 'junk'] })).toEqual(
      [{ path: ['username'], detail: 'too short' }],
    );
    expect(problemFields({ title: 'Bad Request' })).toEqual([]);
  });
});
