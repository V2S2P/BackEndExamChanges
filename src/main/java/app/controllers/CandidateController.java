package app.controllers;

import app.dtos.CandidateDTO;
import app.dtos.TopCandidateDTO;
import app.entities.Candidate;
import app.exceptions.ApiException;
import app.services.CandidateService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CandidateController implements IController{
    private final CandidateService candidateService;
    private static final Logger log = LoggerFactory.getLogger(CandidateController.class);

    // Now whenever a request comes to a candidate endpoint, the controller uses its injected CandidateService
    // to perform logic like getById, etc.
    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @Override
    public Handler create() {
        return (Context ctx) -> {
            try {
                //Request start (what is the point of the process)
                log.info("Request to create a new candidate");
                //When we do this, Javalin uses Jackson under the hood to parse JSON text, create new CandidateDTO object, and set each java field matching with JSON keys(phonenumber, name, etc)
                CandidateDTO candidateDTO = ctx.bodyAsClass(CandidateDTO.class);
                //Details about the request data(DTO)
                log.debug("Candidate request body is {}", candidateDTO);
                CandidateDTO newCandidateDTO = candidateService.create(candidateDTO);
                //Success/Important message
                log.info("Candidate created successfully {}", newCandidateDTO);
                //This is where the serialization happens. We give Javalin a java object (newCandidateDTO) and Javalin internally uses jackson to convert to JSON string.
                ctx.status(HttpStatus.CREATED).json(newCandidateDTO);
            }catch (Exception e) {
                //Exceptions
                log.error("Error while creating a new candidate: {}", e.getMessage());
                throw new ApiException(400, "Invalid request body " + e.getMessage());
            }
        };
    }

    @Override
    public Handler getById() {
        return (Context ctx) -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                CandidateDTO candidateDTO = candidateService.getById(id);
                ctx.status(HttpStatus.OK).json(candidateDTO);
            }catch (NumberFormatException e){
                throw new ApiException(400, "Invalid candidate ID format");
            }
        };
    }

    @Override
    public Handler getAll() {
        return (Context ctx) -> {
            List<CandidateDTO> candidateDTOs = candidateService.getAll();
            ctx.status(HttpStatus.OK).json(candidateDTOs);
        };
    }

    @Override
    public Handler update() {
        return (Context ctx) -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                CandidateDTO candidateDTO = ctx.bodyAsClass(CandidateDTO.class);
                CandidateDTO updatedCandidateDTO = candidateService.update(candidateDTO, id);
                //Could also use NO_CONTENT, but if so, remove the body(the .json)
                ctx.status(HttpStatus.OK).json(updatedCandidateDTO);
            }catch (NumberFormatException e){
                throw new ApiException(400, "Invalid candidate ID format");
            }
        };
    }

    @Override
    public Handler delete() {
        return (Context ctx) -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                candidateService.delete(id);
                //No body since we use NO_CONTENT
                ctx.status(HttpStatus.NO_CONTENT);
            }catch (NumberFormatException e){
                throw new ApiException(400, "Invalid candidate ID format");
            }
        };
    }
    public Handler linkSkill() {
        return (Context ctx) -> {
            try {
                int candidateId = Integer.parseInt(ctx.pathParam("candidateId"));
                int skillId = Integer.parseInt(ctx.pathParam("skillId"));
                CandidateDTO updatedCandidateDTO = candidateService.linkSkill(candidateId, skillId);
                ctx.status(HttpStatus.OK).json(updatedCandidateDTO);
            }catch (ApiException e){
                throw e;
            }catch (Exception e){
                throw new ApiException(400, "Invalid candidate or skill ID format");
            }
        };
    }
    public Handler filterCandidateByCategory() {
        return (Context ctx) -> {
            String category = ctx.pathParam("category");
            if (category == null || category.isBlank()) {
                //Validation issues
                log.warn("Invalid or missing candidate category");
                throw new ApiException(400, "Invalid category. Category query parameter is required");
            }
            List<CandidateDTO> candidateDTOS = candidateService.filterCandidatesByCategory(category);
            ctx.status(HttpStatus.OK).json(candidateDTOS);
        };
    }
    public Handler getByIdEnriched() {
        return ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            CandidateDTO enrichedCandidate = candidateService.getByIdEnriched(id);
            ctx.status(HttpStatus.OK).json(enrichedCandidate);
        };
    }
    public Handler getTopCandidateByPopularityScore() {
        return (Context ctx) -> {
            TopCandidateDTO topCandidateDTO = candidateService.getTopCandidateByPopularityScore();
            ctx.status(HttpStatus.OK).json(topCandidateDTO);
        };
    }
    public Handler patchCandidate(){
        return (Context ctx) -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                CandidateDTO candidateDTO = ctx.bodyAsClass(CandidateDTO.class);
                CandidateDTO updatedCandidate = candidateService.patch(id, candidateDTO);
                ctx.status(HttpStatus.OK).json(updatedCandidate);
            }catch (ApiException e){
                throw e;
            }catch (Exception e){
                throw new ApiException(400, "Invalid candidate ID or data");
            }
        };
    }

}
