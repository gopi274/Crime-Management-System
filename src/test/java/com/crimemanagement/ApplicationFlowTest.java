package com.crimemanagement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationFlowTest {

    private static final String TEST_DB_URL =
            "jdbc:h2:mem:crime_management_it;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";

    static {
        System.setProperty("db.url", TEST_DB_URL);
        System.setProperty("db.username", "sa");
        System.setProperty("db.password", "");
        System.setProperty("db.driver", "org.h2.Driver");
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> TEST_DB_URL);
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private javax.sql.DataSource dataSource;

    @org.junit.jupiter.api.BeforeEach
    void setupDatabase() throws Exception {
        org.springframework.core.io.Resource schema = new org.springframework.core.io.ClassPathResource("schema.sql");
        org.springframework.core.io.Resource data = new org.springframework.core.io.ClassPathResource("justice_graduation_data.sql");
        org.springframework.jdbc.datasource.init.ResourceDatabasePopulator populator = 
            new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(schema, data);
        if (dataSource != null) {
            populator.execute(dataSource);
        }
    }

    @Test
    void publicDashboardAndTrackingEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/public/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openCases").isNumber())
                .andExpect(jsonPath("$.closedCases").isNumber())
                .andExpect(jsonPath("$.activeOfficers").isNumber())
                .andExpect(jsonPath("$.stationsOnline").value(262));

        // Use new BNS format: FIR/2026/KOL/001
        mockMvc.perform(get("/api/public/track/FIR/2026/KOL/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FIR"))
                .andExpect(jsonPath("$.id").value("FIR/2026/KOL/001"))
                .andExpect(jsonPath("$.caseNumber").value("CC/2026/RES-001"));
    }

    @Test
    @SuppressWarnings("null")
    void complaintSubmissionReturnsTrackableId() throws Exception {
        String body = """
                {
                  "description": "Suspected illegal narcotics activity detected near beach junction.",
                  "location": "Kochi Beach"
                }
                """;

        mockMvc.perform(post("/api/public/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId").exists())
                .andExpect(jsonPath("$.routingConfidence").isNumber());
    }

    @Test
    @SuppressWarnings("null")
    void defaultAdminCanLogIn() throws Exception {
        // Generate an admin in the script if not there. 
        // Actually, the generator adds court/forensic/admin? No, let's check.
        // I'll ensure 'admin'/'admin' is in the SQL.
        String body = """
                {
                  "username": "admin",
                  "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
