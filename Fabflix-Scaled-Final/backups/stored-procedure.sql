use moviedb;

DROP PROCEDURE IF EXISTS add_star_full;
DROP PROCEDURE IF EXISTS add_star;
DROP PROCEDURE IF EXISTS add_movie;
DROP PROCEDURE IF EXISTS add_movie_basic;
DROP PROCEDURE IF EXISTS add_movie_genre;
DROP PROCEDURE IF EXISTS add_movie_star;
DROP PROCEDURE IF EXISTS add_movie_star_simple;

DELIMITER //
CREATE PROCEDURE add_star_full(IN star_id varchar(10), IN star_name varchar(100), IN star_birth int)
BEGIN
	IF NOT EXISTS(SELECT id FROM stars WHERE LOWER(name) = LOWER(star_name)) THEN
        INSERT INTO stars (id, name, birthYear)
		VALUES(star_id, star_name, star_birth);
    END IF;    
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_star(IN star_id varchar(10), IN star_name varchar(100))
BEGIN
	IF NOT EXISTS(SELECT id FROM stars WHERE LOWER(name) = LOWER(star_name)) THEN
        INSERT INTO stars (id, name)
		VALUES(star_id, star_name);
    END IF;  
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_movie(IN movie_id varchar(10), IN movie_title varchar(100), IN movie_year int, IN movie_director varchar(100), IN movie_price double, IN star_name varchar(100), IN star_id varchar(10), IN genre_name varchar(32))
BEGIN
		CALL add_star(star_id, star_name);
    
    IF NOT EXISTS(SELECT genres.id FROM genres WHERE LOWER(genres.name) = LOWER(genre_name)) THEN
		INSERT INTO genres (name)
        VALUES (genre_name);
    END IF;    
	
    IF NOT EXISTS(SELECT movies.title FROM movies WHERE LOWER(movies.title) = LOWER(movie_title) AND movies.year = movie_year AND LOWER(movies.director) = LOWER(movie_director)) THEN
		INSERT INTO movies (id, title, year, director, price)
		VALUES(movie_id, movie_title, movie_year, movie_director, movie_price);
		
		INSERT INTO stars_in_movies (starId, movieId)
		VALUES ((SELECT stars.id FROM stars WHERE stars.name = star_name), movie_id);
		
		INSERT INTO genres_in_movies (genreId, movieId)
		VALUES ((SELECT genres.id FROM genres WHERE genres.name = genre_name), movie_id);
	END IF;
    
    IF NOT EXISTS(SELECT movieId FROM ratings WHERE LOWER(ratings.movieId) = LOWER(movie_id)) THEN
		INSERT INTO ratings (movieId, rating, numVotes)
        VALUES (movie_id, 9.0, 1);
    END IF;
    
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_movie_basic(IN movie_id varchar(10), IN movie_title varchar(100), IN movie_year int, IN movie_director varchar(100), IN movie_price double)
BEGIN  
    IF NOT EXISTS(SELECT movies.title FROM movies WHERE LOWER(movies.title) = LOWER(movie_title) AND movies.year = movie_year AND LOWER(movies.director) = LOWER(movie_director)) THEN
		INSERT INTO movies (id, title, year, director, price)
		VALUES(movie_id, movie_title, movie_year, movie_director, movie_price);
	END IF;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_movie_genre(IN movie_id varchar(10), IN genre varchar(32))
BEGIN  
	IF NOT EXISTS(SELECT genres.id FROM genres WHERE LOWER(genres.name) LIKE LOWER(genre)) THEN
		INSERT INTO genres (name)
        VALUES (genre);
    END IF;
    
    IF NOT EXISTS(SELECT genres.id FROM movies, genres, genres_in_movies WHERE movies.id = genres_in_movies.movieId AND genres.id = genres_in_movies.genreId AND LOWER(genres.name) = LOWER(genre) AND movies.id = movie_id) THEN
		INSERT INTO genres_in_movies (genreId, movieId)
        VALUES ((SELECT genres.id FROM genres WHERE LOWER(genres.name) LIKE LOWER(genre)), movie_id);
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_movie_star(IN movie_id varchar(10), IN star varchar(100))
BEGIN  
    IF NOT EXISTS(SELECT stars.id FROM movies, stars, stars_in_movies WHERE movies.id = stars_in_movies.movieId AND stars.id = stars_in_movies.starId AND LOWER(stars.name) = LOWER(star) AND movies.id = movie_id) THEN
		INSERT INTO stars_in_movies (starId, movieId)
        VALUES ((SELECT stars.id FROM stars WHERE LOWER(stars.name) = LOWER(star)), movie_id);
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_movie_star_simple(IN movie_id varchar(10), IN star varchar(100))
BEGIN  
	INSERT INTO stars_in_movies (starId, movieId)
	VALUES ((SELECT stars.id FROM stars WHERE LOWER(stars.name) = LOWER(star)), movie_id);
END //
DELIMITER ;