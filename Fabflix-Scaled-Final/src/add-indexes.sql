use moviedb;

ALTER TABLE genres ADD UNIQUE INDEX (id);
ALTER TABLE genres ADD INDEX (name);

ALTER TABLE genres_in_movies ADD INDEX (genreId);
ALTER TABLE genres_in_movies ADD INDEX (movieId);

ALTER TABLE movies ADD UNIQUE INDEX (id);
ALTER TABLE movies ADD INDEX (title);
ALTER TABLE movies ADD INDEX (year);
ALTER TABLE movies ADD INDEX (director);

ALTER TABLE ratings ADD INDEX (movieId);

ALTER TABLE stars ADD UNIQUE INDEX (id);
ALTER TABLE stars ADD INDEX (name);

ALTER TABLE stars_in_movies ADD INDEX (starId);
ALTER TABLE stars_in_movies ADD INDEX (movieId);