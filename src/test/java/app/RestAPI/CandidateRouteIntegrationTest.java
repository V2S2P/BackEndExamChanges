package app.RestAPI;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.daos.SkillDAO;
import app.entities.Skill;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CandidateRouteIntegrationTest {

    private static Javalin app;
    private static EntityManagerFactory emf;
    private static int port;

    private static String userToken;
    private static String adminToken;

    private static int candidateId;
    private static int skillId;

    @BeforeAll
    static void setup() {
        // ✅ Enable test mode for Hibernate
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();

        // ✅ Start server in test mode (uses MockSkillStatsService)
        app = ApplicationConfig.startServer(0, emf, true);
        port = app.port();

        // ✅ Configure RestAssured
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1"; // <-- important for context path

        var securityDAO = new app.security.SecurityDAO(emf);

        // ✅ Setup roles & users (ignore if already exists)
        try { securityDAO.createRole("User"); } catch (Exception ignored) {}
        try { securityDAO.createRole("Admin"); } catch (Exception ignored) {}
        try { securityDAO.createUser("TestUser", "pass123"); } catch (Exception ignored) {}
        try { securityDAO.createUser("AdminUser", "pass123"); } catch (Exception ignored) {}
        try { securityDAO.addUserRole("TestUser", "User"); } catch (Exception ignored) {}
        try { securityDAO.addUserRole("AdminUser", "Admin"); } catch (Exception ignored) {}

        // ✅ Login and get tokens
        userToken = given()
                .contentType("application/json")
                .body("{\"username\":\"TestUser\",\"password\":\"pass123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("Token");

        adminToken = given()
                .contentType("application/json")
                .body("{\"username\":\"AdminUser\",\"password\":\"pass123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("Token");

        // ✅ Create a mock skill
        var skillDAO = new SkillDAO(emf);
        Skill skill = new Skill();
        skill.setName("Java");
        skill.setSlug("java");
        skill.setDescription("Mock skill");
        skillDAO.create(skill);
        skillId = skill.getId();
    }

    @AfterAll
    static void teardown() {
        if (app != null) app.stop();
        if (emf != null && emf.isOpen()) emf.close();
    }

    // --- REGISTER / LOGIN ---
    @Test
    @Order(0)
    void testRegisterAndLogin() {
        // Register new user
        // Given = setting up the request(headers, content type, body, etc.)
        given()
                .contentType("application/json")
                .body("{\"username\":\"NewUser\",\"password\":\"pass123\"}")
                // When = sends the request to the endpoint
                .when()
                .post("/auth/register")
                // Then = validates the response(status code, JSON fields, etc.)
                .then()
                // Hamcrest = framework providing readable, natural language assertions (built into JUnit and RestAssured)
                .statusCode(anyOf(is(200), is(201)))
                .body("username", equalTo("NewUser"));

        // Login
        String token = given()
                .contentType("application/json")
                .body("{\"username\":\"NewUser\",\"password\":\"pass123\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("Token");

        Assertions.assertNotNull(token);
    }

    // --- Candidate CRUD ---
    @Test
    @Order(1)
    void testCreateCandidate() {
        candidateId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                        {
                          "name": "Alice Johnson",
                          "phone": "555-1234",
                          "educationBackground": "BSc Computer Science"
                        }
                        """)
                .post("/candidates") // context path is already /api/v1
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("name", equalTo("Alice Johnson"))
                .extract()
                .path("id");
    }

    @Test
    @Order(2)
    void testGetCandidateById() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .get("/candidates/" + candidateId)
                .then()
                .statusCode(200)
                .body("id", equalTo(candidateId))
                .body("name", equalTo("Alice Johnson"));
    }

    @Test
    @Order(3)
    void testUpdateCandidate() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                    {
                      "id": %d,
                      "name": "Alice Updated",
                      "phone": "555-4321",
                      "educationBackground": "MSc Software Engineering"
                    }
                    """.formatted(candidateId))
                .put("/candidates/" + candidateId)
                .then()
                .statusCode(anyOf(is(200), is(204)));

        // Verify update
        given()
                .header("Authorization", "Bearer " + userToken)
                .get("/candidates/" + candidateId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Alice Updated"));
    }

    @Test
    @Order(4)
    void testLinkSkillToCandidate() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .put("/candidates/" + candidateId + "/skills/" + skillId)
                .then()
                .statusCode(anyOf(is(200), is(204)))
                .body("skills.size()", greaterThanOrEqualTo(1))
                .body("skills[0].name", equalTo("Java"));
    }

    @Test
    @Order(5)
    void testGetCandidateByIdEnriched() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .get("/candidates/" + candidateId + "/enriched")
                .then()
                .statusCode(200)
                .body("id", equalTo(candidateId))
                .body("skills[0].slug", equalTo("java"))
                .body("skills[0].popularityScore", equalTo(93))
                .body("skills[0].averageSalary", equalTo(120000));
    }

    @Test
    @Order(6)
    void testGetTopCandidateByPopularity() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .get("/reports/candidates/top-by-popularity")
                .then()
                .statusCode(200)
                .body("candidateId", equalTo(candidateId))
                .body("averagePopularityScore", equalTo(93.0f));
    }

    @Test
    @Order(7)
    void testDeleteCandidate() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .delete("/candidates/" + candidateId)
                .then()
                .statusCode(anyOf(is(200), is(204)));

        // Verify deletion
        given()
                .header("Authorization", "Bearer " + userToken)
                .get("/candidates/" + candidateId)
                .then()
                .statusCode(404);
    }
}
