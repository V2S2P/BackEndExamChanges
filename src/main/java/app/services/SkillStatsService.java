package app.services;

import app.dtos.SkillStatsDTO;
import app.dtos.SkillStatsResponse;
import app.exceptions.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class SkillStatsService {

    private static final String API_URL = "https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats?slugs=";
    private final ObjectMapper objectMapper;

    public SkillStatsService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<SkillStatsDTO> getSkillStats(List<String> slugs) throws Exception {
        if(slugs.isEmpty()) return List.of();
        try{
        String url = API_URL + String.join(",", slugs);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            throw new ApiException(response.statusCode(), "Failed to fetch skill stats: " + response.body());
        }

        SkillStatsResponse statsResponse = objectMapper.readValue(response.body(), SkillStatsResponse.class);
        return statsResponse.getData();
    }catch (ApiException e){
        throw e;
    }catch(Exception e){
        throw new ApiException(500, "Error contacting skill stats API: " + e.getMessage());}
    }
}
