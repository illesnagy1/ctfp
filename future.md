

```
// ══════════════════════════════════════════════
//  EXTRA FIELDS
// ══════════════════════════════════════════════

Table extra_fields {
  id int [pk, increment]
  name varchar(64) [not null]
  type enum('text', 'textarea', 'number', 'date', 'select', 'radio', 'checkbox', 'file') [not null]
  required_at enum('required', 'optional', 'hidden') [not null, default: 'optional']
  is_unique boolean [not null, default: false]
  regex_validation varchar(512)
  is_public boolean [not null, default: false]
  is_visible_to_user boolean [not null, default: true]
  is_editable boolean [not null, default: true]
  description text
  placeholder varchar(255)
  sort_order int [not null, default: 0]
  created_at timestamp [not null, default: `now()`]
  updated_at timestamp [not null, default: `now()`]
}

Table extra_field_options {
  id int [pk, increment]
  field_id int [not null, ref: > extra_fields.id]
  label varchar(255) [not null]
  value varchar(255) [not null]
  sort_order int [not null, default: 0]

  Note: 'Options for select/radio type fields'
}

Table extra_field_values {
  id int [pk, increment]
  field_id int [not null, ref: > extra_fields.id]
  user_id int [not null, ref: > users.id]
  value text

  indexes {
    (field_id, user_id) [unique]
  }
}

// Auth

Table oauth_providers {
  id int [pk, increment]
  name varchar(64) [unique, not null]
  type enum('google', 'github', 'custom') [not null]
  client_id varchar(255) [not null]
  client_secret varchar(255) [not null]
  authorize_url varchar(512)
  token_url varchar(512)
  userinfo_url varchar(512)
  scopes varchar(512)
  is_enabled boolean [not null, default: true]
  created_at timestamp [not null, default: `now()`]
  updated_at timestamp [not null, default: `now()`]
}

Table oauth_accounts {
  id int [pk, increment]
  user_id int [not null, ref: > users.id]
  provider_id int [not null, ref: > oauth_providers.id]
  provider_user_id varchar(255) [not null]
  access_token text
  refresh_token text
  expires_at timestamp
  created_at timestamp [not null, default: `now()`]

  indexes {
    (provider_id, provider_user_id) [unique]
  }
}

```
Half-Life Notification TTS
Webhooks
Social media shares
