package app.controllers;

import app.dtos.SkillDTO;
import app.services.SkillService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import java.util.List;

public class SkillController implements IController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    // CREATE
    @Override
    public Handler create() {
        return (Context ctx) -> {
            SkillDTO skillDTO = ctx.bodyAsClass(SkillDTO.class);
            SkillDTO createdSkill = skillService.create(skillDTO);
            ctx.status(HttpStatus.CREATED).json(createdSkill);
        };
    }

    // READ BY ID
    @Override
    public Handler getById() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            SkillDTO skillDTO = skillService.getById(id);
            ctx.status(HttpStatus.OK).json(skillDTO);
        };
    }

    // READ ALL
    @Override
    public Handler getAll() {
        return (Context ctx) -> {
            List<SkillDTO> skills = skillService.getAll();
            ctx.status(HttpStatus.OK).json(skills);
        };
    }

    // UPDATE
    @Override
    public Handler update() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            SkillDTO skillDTO = ctx.bodyAsClass(SkillDTO.class);
            SkillDTO updatedSkiDTO = skillService.update(skillDTO, id);
            ctx.status(HttpStatus.OK).json(updatedSkiDTO);
        };
    }

    // DELETE
    @Override
    public Handler delete() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            skillService.delete(id);
            ctx.status(HttpStatus.NO_CONTENT);
        };
    }
}
