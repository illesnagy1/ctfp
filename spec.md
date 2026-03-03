# CTF+ specifications

Register:

- username
- email (optional) - config in admin
- password (optional) - config in admin
- confirm password (if password is set)
- agree to terms/rules (if it exists)
- agree to privacy policy (if it exists)
- registration code (if it exists)
- country (optional) - config in admin
- extra fields - config in admin
- email link/manual verification - config in admin

Login methods:

- email/username + password
- email + magic link/code
- remember me checkbox
- forgot password link (if password is set)
- TOTP 2FA (if enabled) - config in admin
- OAuth2 (Google/GitHub/Custom) - config in admin

Authentication method:

- Session by default
- JWT token (for API) - config in admin

Extra fields:

- text/textarea/number/date/select/radio/checkbox/file
- required/optional/hidden at registration
- unique/non-unique
- regex validation
- public/private for others
- visible/hidden for user
- can be edited by user
- description/placeholder

Notifications:

- title
- message (markdown/html)
- type (info/success/warning/danger)
- native browser notification (if user allows) + toast + notification tab
- send in email checkbox (if email enabled)
- play custom sound (upload in admin) or [HL TTS](https://tz-dev.github.io/hl_text2speech/index.html)
- notify about time left with hl tts checkbox
- user can disable/enable sound in profile

Scoreboard:

- has a view for users and for admins
- can be disabled for users in admin
- search bar (look for username/team name)
- pagination
- sort by all columns
- export to csv button
- Team tab (if enabled):
  - rank
  - name
  - score
  - challenges solved
  - country flag (if enabled)
- User tab:
  - rank
  - name
  - score
  - challenges solved
  - country flag (if enabled)
- each row is clickable, leads to user/team profile
- admin view:
  - shows if verified (green background)
  - shows hidden (gray background)
  - bulk select checkbox (for delete only)
  - create user/team button (opens modal with register form)
  - edit user/team button (opens modal with edit form)
  - delete (ban) user/team button (opens modal with delete confirmation, has to type in name to confirm)

Challenge:

- title
- description (markdown/html)
- categories (can be in multiple)
- attachments (multiple)
- visibility (visible/hidden)
- time to publicate
- max attempts (optional)
- time limit (optional) (only visible after a start button)
- hints (multiple)
  - title
  - description (markdown/html)
  - cost (optional)
  - visible after (optional)
- submissions:
  - user/team
  - flag
  - correct/incorrect
  - time
  - ip address
  - user agent
  - admin view:
    - mark as correct/incorrect
    - bulk select checkbox (for edit)
- admin view:
  - edit challenge
  - delete challenge (opens modal with delete confirmation, has to type in title to confirm)
  - bulk select checkbox (for delete)
- Flags:
  - regex (solves static, multiple answer, case-sensitiveness)
  - can be added more
  - title for each (optional)
  - initial value
  - decay value
  - minimum value
  - decay function (static/linear/logarithmic/parabolic)
  - current value (calculated)
- TODO: score by flags or by challenges

User profile view:

- username
- email (if enabled) - config in admin
- change password (if enabled)
- country (optional) - config in admin
- extra fields - config in admin
- team (if enabled)
- score
- ranking
- sessions
- logout session
- visible for others checkbox for each field
- admin view:
  - delete (ban) user - send email if configured
  - verify/unverify
  - hide/unhide
  - role
  - submissions
  - solved flags

Team profile view:

- members

Custom pages:

- admin view: create/edit/delete page, bulk select checkbox (for delete)
- title
- language
- theme type (light/dark)
- route
- format (md/html/asciidoc/typst)
- target (current/new tab)
- body
- draft, hidden, auth checkbox
- includable variables
- uploadable files
- text/preview views
- list pages view
- visible pages should be shown in header for users
- custom page can exist with same route for different languages/themes

Config:

- event name, description
- favicon, logo
- start, end, freeze time
- pause
- default theme type (light/dark/system)
- custom themes
- default language
- import/export (json/yaml)
- reset
- robots.txt
- ToS page
- Privacy Policy page
- registration options, extra fields
- authentication options
- user/team options
- email config, email templates
- webhooks notifications
- social share templates
- handle concurrency

- yaml and json (read-only) view on every page where can configure something with monaco editor, the json represents the data will be sent to the backend
