package fr.isen.java2.db.daos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;

import javax.sql.DataSource;

public class MovieDao {

	DataSource dataSource;
	GenreDao genreDao;
	Connection connection;

	public MovieDao()
	{
		dataSource = DataSourceFactory.getDataSource();
		genreDao = new GenreDao();
	}

	public List<Movie> listMovies() {
		List<Movie> movies = new ArrayList<Movie>();
		String prompt = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre";
		try
		{
			connection = dataSource.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(prompt);
			while (resultSet.next())
			{
				movies.add(new Movie(
						resultSet.getInt("idmovie"),
						resultSet.getString("title"),
						//.toLocalDate() in movie class Date = LocalDate
						resultSet.getDate("release_date").toLocalDate(),
						new Genre(resultSet.getInt("idgenre"),resultSet.getString("name")),
						resultSet.getInt("duration"),
						resultSet.getString("director"),
						resultSet.getString("summary")
				));
			}
			connection.close();
			return movies;
			//connection close because 2 system(2 sessions) have run in the same time to have close and connect again
		}
		catch (java.sql.SQLException e)
		{
			throw new RuntimeException("SQL Error at MovieDao.listMovies()");
		}
	}

	public List<Movie> listMoviesByGenre(String genreName) {
		List<Movie> movies = new ArrayList<Movie>();
		String prompt = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre WHERE genre.name = '" + genreName +"'";
		try
		{
			connection = dataSource.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(prompt);
			while (resultSet.next())
			{
				movies.add(new Movie(
						resultSet.getInt("idmovie"),
						resultSet.getString("title"),
						resultSet.getDate("release_date").toLocalDate(),
						new Genre(resultSet.getInt("idgenre"),resultSet.getString("name")),
						resultSet.getInt("duration"),
						resultSet.getString("director"),
						resultSet.getString("summary")
				));
			}
			connection.close();
			return movies;
		}
		catch (java.sql.SQLException e)
		{
			throw new RuntimeException("SQL Error at MovieDao.listMoviesByGenre()");
		}
	}

	public Movie addMovie(Movie movie) {
		String prompt = "INSERT INTO movie(title,release_date,genre_id,duration,director,summary) VALUES(?,?,?,?,?,?)";

		try
		{
			connection = dataSource.getConnection();
			PreparedStatement statement = connection.prepareStatement(prompt);

			statement.setString(1, movie.getTitle());

			//Recheck Usable?
			statement.setObject(2,movie.getReleaseDate());
			//getGenreDao and GetID because want only data of ID in Genre
			statement.setInt(3,movie.getGenre().getId());
			statement.setInt(4,movie.getDuration());
			statement.setString(5,movie.getDirector());
			statement.setString(6,movie.getSummary());

			statement.executeUpdate();
			//want to retrieve the generate ID
			ResultSet ids = statement.getGeneratedKeys();

			//must return value
			//can not find that ID
			if (!ids.next())
			{
				throw new SQLException("Failed to create Movie");
			}
			movie.setId(ids.getInt(1));
			Movie returnMovie = new Movie(
					ids.getInt(1),
					movie.getTitle(),
					movie.getReleaseDate(),
					movie.getGenre(),
					movie.getDuration(),
					movie.getDirector(),
					movie.getSummary()
			);
			connection.close();
			return returnMovie;
		}
		catch (java.sql.SQLException e)
		{
			throw new RuntimeException("SQL Error at MovieDao.addMovie()");
		}
	}
}
