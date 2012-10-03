# MacroEntry schema

# --- !Ups
ALTER TABLE beefcake
ALTER COLUMN password VARCHAR(256)


# --- !Downs
ALTER TABLE beefcake
ALTER COLUMN password VARCHAR(64)

