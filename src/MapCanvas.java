import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.*;

public class MapCanvas extends Canvas
{
    private GraphicsContext gc = null;
    private Connection conn = null;
    private PreparedStatement cities = null;
    private PreparedStatement roads = null;
    private PreparedStatement max_size = null;
    private ResultSet rs = null;


    public MapCanvas(double width, double height) // get map name here
    {
        super(900,700);

        // initialize connection and prepared statements
        this.initializeConn();
        this.setPreparedStatements();

        // Get the graphics context for the canvas & clear.
        gc = getGraphicsContext2D();
        clear();
    }

    public void show(String mapName)
    {
        this.clear();

        try
        {
            this.cities.setString(1,mapName);
            this.rs = this.cities.executeQuery();
            while(this.rs.next())
            {
                this.gc.strokeOval(this.rs.getDouble(2),this.rs.getDouble(3),5,5);
            }
        }
        catch(SQLException e)
        {
            new Alert(Alert.AlertType.ERROR,"Error getting map information from database!").showAndWait();
        }

    }


    public void clear()
    {
        gc.clearRect(0, 0, this.gc.getCanvas().getWidth(), this.gc.getCanvas().getHeight());
        gc.setStroke(Color.LIGHTSLATEGRAY);
        gc.strokeRect(0, 0, this.gc.getCanvas().getWidth(), this.gc.getCanvas().getHeight());
    }

    private void resized()
    {
        System.out.println("resized!");
    }

    private void initializeConn()
    {
        try
        {
            // initiate connection to database
            this.conn = DriverManager.getConnection(Main.CONN_STRING);
        } catch(SQLException e)
        {
            // in case of an error show error message and quit the application
            new Alert(Alert.AlertType.ERROR,
                      "Error initializing connection to the database!",
                      ButtonType.CLOSE)
                    .showAndWait();
            Platform.exit();
        }
    }

    private void setPreparedStatements()
    {
        if(this.conn == null)
            this.initializeConn();

        try
        {
            this.cities = this.conn.prepareStatement("SELECT M.Name,C.PosX,C.PosY " +
                                                     "FROM CITY C " +
                                                     "INNER JOIN MAP M ON M.ID = C.MapID " +
                                                     "WHERE M.Name = ?");

            this.max_size = this.conn.prepareStatement("SELECT MAX(C.PosX), MAX(C.PosY) " +
                                                       "FROM CITY C " +
                                                       "INNER JOIN MAP M ON M.ID=C.MapID " +
                                                       "WHERE M.Name = ?");
        }
        catch(SQLException e)
        {
            new Alert(Alert.AlertType.ERROR,"Error loading city information!", ButtonType.CLOSE).showAndWait();
            Platform.exit();
        }
    }

    public void destroy()
    {
        try
        {
            if(this.cities != null)
                this.cities.close();

            if(this.roads != null)
                this.roads.close();

            if(this.conn != null)
            {
                this.conn.close();
            }
        }
        catch(SQLException e)
        {
            // we tried
        }
    }

}