CREATE DATABASE IF NOT EXISTS moviedb;

USE moviedb;

CREATE TABLE movies (
        id varchar(10) PRIMARY KEY,
        title varchar(100) NOT NULL,
        year int NOT NULL,
        director varchar(100) NOT NULL
);

CREATE INDEX moviesIndex on movies(id);

create table stars (
        id varchar(10) PRIMARY KEY,
        name varchar(100) NOT NULL,
        birthYear int
);

CREATE INDEX starsIndex on stars(id);

create table stars_in_movies (
        starId varchar(10) NOT NULL REFERENCES stars(id),
        movieId varchar(10) NOT NULL REFERENCES movies(id)
);

CREATE INDEX simIndex on stars_in_movies(starId);

create table genres (
        id int AUTO_INCREMENT PRIMARY KEY,
        name varchar(32) NOT NULL
);

CREATE INDEX genresIndex on genres(id);

create table genres_in_movies (
        genreId int NOT NULL REFERENCES genres(id),
        movieId varchar(10) NOT NULL REFERENCES movies(id)
);

CREATE INDEX gimIndex on genres_in_movies(genreId);

create table customers (
        id int AUTO_INCREMENT PRIMARY KEY,
        firstName varchar(50) NOT NULL,
        lastName varchar(50) not null,
        ccId varchar(20) NOT NULL REFERENCES creditcards(id),
        address varchar(200) NOT NULL,
        email varchar(50) NOT NULL,
        password varchar(20) NOT NULL
);

CREATE INDEX customersIndex on customers(id);

create table sales (
        id int AUTO_INCREMENT PRIMARY KEY,
        customerId int NOT NULL REFERENCES customers(id),
        movieId varchar(10) NOT NULL REFERENCES movies(id),
        saleDate date NOT NULL
);

CREATE INDEX salesIndex on sales(id);

create table creditcards (
        id varchar(20) PRIMARY KEY,
        firstName varchar(50) NOT NULL,
        lastName varchar(50) NOT NULL,
        expiration date NOT NULL
);

CREATE INDEX ccIndex on creditcards(id);

create table ratings (
        movieId varchar(10) NOT NULL REFERENCES movies(id),
        rating float NOT NULL,
        numVotes int NOT NULL
);

CREATE INDEX ratingsIndex on ratings(movieId);
