package app.config;

import app.routes.CandidateRoutes;
import app.routes.SkillRoutes;
import app.security.SecurityRoutes;
import io.javalin.apibuilder.EndpointGroup;

public class RoutesRegistry {

    private final CandidateRoutes candidateRoutes;
    private final SkillRoutes skillRoutes;
    private final SecurityRoutes securityRoutes;

    // Each route group here now depends on a service. No new services are created here, they're all reused from
    // ServiceRegistry.
    public RoutesRegistry(ServiceRegistry services) {
        this.candidateRoutes = new CandidateRoutes(services.candidateService);
        this.skillRoutes = new SkillRoutes(services.skillService);
        this.securityRoutes = new SecurityRoutes();
    }

    public EndpointGroup getRoutes() {
        return () -> {
            candidateRoutes.getRoutes().addEndpoints();
            skillRoutes.getRoutes().addEndpoints();
            securityRoutes.getSecurityRoutes().addEndpoints();
            SecurityRoutes.getSecuredRoutes().addEndpoints();

        };
    }
}
