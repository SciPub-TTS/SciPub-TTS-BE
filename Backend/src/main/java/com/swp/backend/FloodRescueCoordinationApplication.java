package com.swp.backend;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FloodRescueCoordinationApplication {

	public static void main(String[] args) {
		applyDatasourceDefaultsFromEnvironment();
		SpringApplication.run(FloodRescueCoordinationApplication.class, args);
	}

	private static void applyDatasourceDefaultsFromEnvironment() {
		String dbUrl = firstNonBlank(System.getenv("DB_URL"), System.getenv("DATABASE_URL"));
		String dbUsername = firstNonBlank(System.getenv("DB_USERNAME"));
		String dbPassword = firstNonBlank(System.getenv("DB_PASSWORD"));
		ParsedMysqlUrl parsedMysqlFromUrl = null;

		if (!isBlank(dbUrl) && dbUrl.startsWith("mysql://")) {
			parsedMysqlFromUrl = parseMysqlUrl(dbUrl);
			if (parsedMysqlFromUrl != null) {
				dbUrl = parsedMysqlFromUrl.jdbcUrl;
				dbUsername = firstNonBlank(dbUsername, parsedMysqlFromUrl.username);
				dbPassword = firstNonBlank(dbPassword, parsedMysqlFromUrl.password);
			}
		}

		if (isBlank(dbUrl)) {
			String mysqlUrl = firstNonBlank(System.getenv("MYSQL_URL"));
			if (!isBlank(mysqlUrl)) {
				parsedMysqlFromUrl = parseMysqlUrl(mysqlUrl);
				if (parsedMysqlFromUrl != null) {
					dbUrl = parsedMysqlFromUrl.jdbcUrl;
					dbUsername = firstNonBlank(dbUsername, parsedMysqlFromUrl.username);
					dbPassword = firstNonBlank(dbPassword, parsedMysqlFromUrl.password);
				}
			}
		}

		if (parsedMysqlFromUrl == null) {
			String mysqlUrl = firstNonBlank(System.getenv("MYSQL_URL"));
			if (!isBlank(mysqlUrl)) {
				parsedMysqlFromUrl = parseMysqlUrl(mysqlUrl);
			}
		}

		if (parsedMysqlFromUrl != null) {
			dbUsername = firstNonBlank(dbUsername, parsedMysqlFromUrl.username);
			dbPassword = firstNonBlank(dbPassword, parsedMysqlFromUrl.password);
		}

		if (isBlank(dbUrl)) {
			String mysqlHost = firstNonBlank(System.getenv("MYSQLHOST"), System.getenv("MYSQL_HOST"));
			String mysqlPort = firstNonBlank(System.getenv("MYSQLPORT"), System.getenv("MYSQL_PORT"), "3306");
			String mysqlDatabase = firstNonBlank(System.getenv("MYSQLDATABASE"), System.getenv("MYSQL_DATABASE"), "railway");
			if (!isBlank(mysqlHost)) {
				dbUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase
						+ "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
				dbUsername = firstNonBlank(dbUsername, System.getenv("MYSQLUSER"), System.getenv("MYSQL_USER"), "root");
				dbPassword = firstNonBlank(dbPassword, System.getenv("MYSQLPASSWORD"), System.getenv("MYSQL_PASSWORD"));
			}
		}

		if (!isBlank(dbUrl)) {
			System.setProperty("spring.datasource.url", dbUrl);
		}
		if (!isBlank(dbUsername)) {
			System.setProperty("spring.datasource.username", dbUsername);
		}
		if (dbPassword != null) {
			System.setProperty("spring.datasource.password", dbPassword);
		}
	}

	private static ParsedMysqlUrl parseMysqlUrl(String mysqlUrl) {
		try {
			URI uri = URI.create(mysqlUrl);
			if (!"mysql".equalsIgnoreCase(uri.getScheme())) {
				return null;
			}

			String userInfo = uri.getUserInfo();
			String username = null;
			String password = null;
			if (!isBlank(userInfo)) {
				String[] parts = userInfo.split(":", 2);
				username = decode(parts[0]);
				if (parts.length > 1) {
					password = decode(parts[1]);
				}
			}

			String host = uri.getHost();
			int port = uri.getPort() > 0 ? uri.getPort() : 3306;
			String database = uri.getPath();
			if (!isBlank(database) && database.startsWith("/")) {
				database = database.substring(1);
			}
			if (isBlank(database)) {
				database = "railway";
			}

			String query = uri.getQuery();
			String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
			if (isBlank(query)) {
				jdbcUrl += "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
			} else {
				jdbcUrl += "?" + query;
			}

			return new ParsedMysqlUrl(jdbcUrl, username, password);
		} catch (Exception ignored) {
			return null;
		}
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private static String firstNonBlank(String... values) {
		if (values == null) {
			return null;
		}
		for (String value : values) {
			if (!isBlank(value)) {
				return value;
			}
		}
		return null;
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private record ParsedMysqlUrl(String jdbcUrl, String username, String password) {
	}

}
