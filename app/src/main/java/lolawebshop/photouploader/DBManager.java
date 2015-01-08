package lolawebshop.photouploader;

import android.util.Log;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonathan on 08/01/2015.
 */

public class DBManager {

    static Connection c;

    public static void makeConnection(String databaseURL) {
        try {
            URI dbUri = new URI(databaseURL);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() +
                    dbUri.getPath() + "?user=" + username + "&password=" +
                    password + "&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(dbUrl);
        } catch (Exception e){
            Log.e("POSTGRESQL", "ERROR! " + e.getMessage(), e);
        }
    }

    public static void insertarPostgreSQL(String titulo, String proveedor, int categoria, Map upload) {
        
        if (titulo != null && proveedor != null && upload != null) {
            PreparedStatement query = null;
            try {
                String imagen = upload.get("public_id").toString();
                query = c.prepareStatement("INSERT INTO lola.productos (titulo, proveedor, categoria, imagen) VALUES (?,?,?,?)");
                query.setString(1, titulo);
                query.setString(2, proveedor);
                query.setInt(3, categoria);
                query.setString(4, imagen);

                int retorno = query.executeUpdate();
                if (retorno > 0)
                    Log.i("POSTGRESQL", "Insertado correctamente");

            }catch(SQLException sqle){
                Log.e("POSTGRESQL", "SQLState: "
                        + sqle.getSQLState() + " SQLErrorCode: "
                        + sqle.getErrorCode(), sqle);
            }catch(Exception e){
                Log.e("POSTGRESQL", "ERROR ", e);
            }finally{
                if (query != null) {
                    try {
                        query.close();
                    } catch (Exception e) {
                        Log.e("POSTGRESQL", "ERROR: Closing query ", e);
                    }
                }
            }
        }
        else {
            Log.e("FORM", "ERROR: Algun atributo es nulo");
        }
    }

    //TODO reemplazarlo por una consulta a la base de datos de las categorias disponibles
    public static Map<String, Integer> getCategorias() {
        Map<String, Integer> categorias = new HashMap<String, Integer>();
        categorias.put("aros", 1);
        categorias.put("billeteras", 2);
        categorias.put("cintos", 3);
        categorias.put("chalinas", 4);
        categorias.put("clutchs", 5);
        categorias.put("collares", 6);
        categorias.put("monederos", 7);
        categorias.put("portacelulares", 8);
        categorias.put("pulseras", 9);
        categorias.put("relojes", 10);
        categorias.put("sombreros", 11);
        categorias.put("anillos", 12);
        return categorias;
    }
}
