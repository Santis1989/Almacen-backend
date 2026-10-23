
package ies.belgrano.almacen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class AlmacenApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() {
		assertNotNull(dataSource);
	}

	@Test
	void conexionUtilizaH2() throws SQLException {

		try (Connection conexion = dataSource.getConnection()) {

			String url = conexion.getMetaData().getURL();

			assertTrue(
					url.startsWith("jdbc:h2:"),
					"La prueba debe utilizar H2 y no MySQL");
		}
	}
}
