package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static void main(String[] args) {
        //We create one emf to be shared by all. We do this because it is expensive in resources to create EMFs.
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        ApplicationConfig.startServer(7070, emf);
    }
}
