package edu.cu.hcms.web;

import edu.cu.hcms.dao.Seed;
import edu.cu.hcms.dao.UserDao;
import edu.cu.hcms.util.Db;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.SQLException;

/** Creates the database tables (and demo data on the first run) when the application starts. */
@WebListener
public class AppInit implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Db.init();
            if (new UserDao().count() == 0) {
                Seed.run();
                sce.getServletContext().log("Demo data created.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database initialisation failed", e);
        }
    }
}
