package app.config;

import app.exceptions.ApiException;
import app.security.SecurityController;
import app.services.CandidateService;
import app.services.MockSkillStatsService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

public class ApplicationConfig {
    private static Javalin app;

    /**
     * Start server (normal mode)
     */
    public static Javalin startServer(int port, EntityManagerFactory emf) {
        return startServer(port, emf, false);
    }

    /**
     * Start server with optional test mode
     */
    public static Javalin startServer(int port, EntityManagerFactory emf, boolean testMode) {
        // ServiceRegistry gets the emf, which now means all the services and daos will have access to it.
        ServiceRegistry services = new ServiceRegistry(emf);

        // ✅ Use mock service in test mode. This is dependency injection. Swapping one implementation for another.
        if (testMode) {
            services.setCandidateService(new CandidateService(emf, new MockSkillStatsService()));
        }
        // RoutesRegistry receives the ServiceRegistry so it can pass the needed services to each route group.
        RoutesRegistry routes = new RoutesRegistry(services);

        app = Javalin.create(config -> configure(config, routes));

        // ✅ Global exception handler
        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getStatusCode()).json(Map.of(
                    "status", e.getStatusCode(),
                    "message", e.getMessage()
            ));
        });

        // ✅ Always attach security
        SecurityController securityController = new SecurityController();
        app.beforeMatched(securityController.authenticate());
        app.beforeMatched(securityController.authorize());

        app.start(port);
        return app;
    }

    /**
     * Configure Javalin
     */
    private static void configure(JavalinConfig config, RoutesRegistry routes) {
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes");

        // ✅ Always set context path for consistency
        config.router.contextPath = "/api/v1";

        config.router.apiBuilder(routes.getRoutes());
    }

    /**
     * Stop server and close EMF
     */
    public static void stopServer() {
        if (app != null) {
            System.out.println("Stopping server and closing EMF...");
            app.stop();
            if (HibernateConfig.getEntityManagerFactory().isOpen()) {
                HibernateConfig.getEntityManagerFactory().close();
            }
        }
    }
}
