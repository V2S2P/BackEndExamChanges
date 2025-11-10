package app.routes;

import app.controllers.CandidateController;
import app.security.Roles;
import app.services.CandidateService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CandidateRoutes {
    private final CandidateController candidateController;

    // CandidateController now receives its service dependency. CandidateService is injected into CandidateController
    public CandidateRoutes(CandidateService candidateService) {
        this.candidateController = new CandidateController(candidateService);
    }

    public EndpointGroup getRoutes() {
        return () -> {
            path("candidates", () -> {
                post(candidateController.create(), Roles.ADMIN); // create new candidate
                get(candidateController.getAll(), Roles.ADMIN, Roles.USER);

                path("category/{category}", () -> {
                    get(candidateController.filterCandidateByCategory(), Roles.ADMIN, Roles.USER); // Filter candidates by skill category
                });

                path("{id}", () -> {
                    get(candidateController.getById(), Roles.USER, Roles.ADMIN);   // get candidate by id
                    put(candidateController.update(), Roles.ADMIN);    // update candidate
                    delete(candidateController.delete(), Roles.ADMIN); // delete candidate
                    patch(candidateController.patchCandidate(), Roles.ADMIN); // patch candidate
                });

                path("{candidateId}/skills/{skillId}", () -> {
                    put(candidateController.linkSkill(), Roles.ADMIN); // link existing skill to candidate
                });
                path("{id}/enriched", () -> {
                    get(candidateController.getByIdEnriched(), Roles.USER, Roles.ADMIN);
                });
            });
            path("reports", () -> {
                path("candidates", () -> {
                    path("top-by-popularity", () -> {
                        get(candidateController.getTopCandidateByPopularityScore(), Roles.USER, Roles.ADMIN);
                    });
                });
            });
        };
    }
}
