# MacroEntry schema

# --- !Ups
CREATE TABLE beefcake (
	username VARCHAR(32) NOT NULL PRIMARY KEY,
	password VARCHAR(256) NOT NULL,
	email VARCHAR(64) NOT NULL UNIQUE,
	adhoc BOOLEAN NOT NULL,
	lastUpdated LONG NOT NULL
);

CREATE SEQUENCE food_id_seq;
CREATE TABLE food (
    id INTEGER NOT NULL DEFAULT nextval('food_id_seq') PRIMARY KEY,
	name VARCHAR(256) NOT NULL UNIQUE,
	kCal INTEGER NOT NULL,
	protein FLOAT NOT NULL,
	fat FLOAT NOT NULL,
	carbs FLOAT NOT NULL,
    username VARCHAR(32) REFERENCES beefcake(username)
);

CREATE SEQUENCE macro_id_seq;
CREATE TABLE macroEntry (
	id INTEGER NOT NULL DEFAULT nextval('macro_id_seq') PRIMARY KEY,
	username VARCHAR(32) NOT NULL REFERENCES beefcake(username),
	food VARCHAR(256),
	day INTEGER NOT NULL,
	month INTEGER NOT NULL,
	year INTEGER NOT NULL,
	amount INTEGER NOT NULL,
	kCal FLOAT NOT NULL,
	protein FLOAT NOT NULL,
	fat FLOAT NOT NULL,
	carbs FLOAT NOT NULL 
);

CREATE SEQUENCE measure_id_seq;
CREATE TABLE measure (
	id INTEGER NOT NULL DEFAULT nextval('measure_id_seq') PRIMARY KEY,
	username VARCHAR(32) NOT NULL REFERENCES beefcake(username),
	day INTEGER NOT NULL,
	month INTEGER NOT NULL,
	year INTEGER NOT NULL,
	field VARCHAR(32) NOT NULL,
	value FLOAT NOT NULL,
	CONSTRAINT one_entry UNIQUE (day, month, year, field, username)
);

CREATE TABLE measureGoal (
	username VARCHAR(32) NOT NULL REFERENCES beefcake(username),
	field VARCHAR(32) NOT NULL,
	goalValue FLOAT NOT NULL,
	CONSTRAINT one_goal UNIQUE (username, field)
);

# --- !Downs

