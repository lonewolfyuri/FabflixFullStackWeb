use moviedb;

ALTER TABLE movies ADD COLUMN price DOUBLE DEFAULT 4.99;

UPDATE movies
INNER JOIN ratings ON movies.id = ratings.movieId
SET price = CASE
	WHEN rating >= 7.5 THEN 24.99
    WHEN rating < 7.5 AND rating >= 6.0 THEN 19.99
    WHEN rating < 6.0 AND rating >= 5.0 THEN 14.99
    WHEN rating < 5.0 AND rating >= 2.5 THEN 9.99
    ELSE 4.99
END;    