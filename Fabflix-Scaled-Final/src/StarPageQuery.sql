use moviedb;

SELECT stars.id, stars.name, stars.birthYear, GROUP_CONCAT(movies.title) as movieTitles, GROUP_CONCAT(movies.id) as movieIds
FROM stars, stars_in_movies, movies
WHERE stars.id = stars_in_movies.starId
	AND movies.id = stars_in_movies.movieId
GROUP BY stars.id, stars.name;