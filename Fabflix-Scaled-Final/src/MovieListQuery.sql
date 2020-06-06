use moviedb;

SELECT movies.id, movies.title, movies.year, movies.director, genreNames as genres, genreIds, starNames as stars, starIds, rating
FROM movies, ratings,
	(SELECT movieId, GROUP_CONCAT(starId) as starIds, GROUP_CONCAT(name) as starNames
	FROM stars, stars_in_movies
	WHERE stars.id = stars_in_movies.starId
	GROUP BY movieId
	) as movies_with_stars,
	(SELECT movieId, GROUP_CONCAT(genreId) as genreIds, GROUP_CONCAT(name) as genreNames
	FROM genres, genres_in_movies
	WHERE genres.id = genres_in_movies.genreId
	GROUP BY movieId
	) as movies_with_genres
WHERE movies.id = movies_with_stars.movieId
	AND movies.id = movies_with_genres.movieId
	AND ratings.movieId = movies.id
ORDER BY rating DESC
LIMIT 20;

/*
SELECT *
FROM movies, genres, genres_in_movies, stars, stars_in_movies, ratings
WHERE movies.id = genres_in_movies.movieId
	AND movies.id = stars_in_movies.movieId
    AND movies.id = ratings.movieId
    AND stars.id = stars_in_movies.starId
    AND genres.id = genres_in_movies.genreId;
*/