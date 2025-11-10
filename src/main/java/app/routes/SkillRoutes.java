package app.routes;

import app.controllers.SkillController;
import app.security.Roles;
import app.services.SkillService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SkillRoutes {
    private final SkillController skillController;

    public SkillRoutes(SkillService skillService) {
        this.skillController = new SkillController(skillService);
    }

    public EndpointGroup getRoutes() {
        return () -> {
            path("skills", () -> {
                post(skillController.create(), Roles.ADMIN); // create skill
                get(skillController.getAll(), Roles.USER);  // get all skills

                path("{id}", () -> {
                    get(skillController.getById(), Roles.USER);   // get skill by id
                    put(skillController.update(), Roles.ADMIN);    // update skill
                    delete(skillController.delete(), Roles.ADMIN); // delete skill
                });
            });
        };
    }
}
