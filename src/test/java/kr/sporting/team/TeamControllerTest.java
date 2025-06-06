package kr.sporting.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.sporting.H2TestDatabaseConfig;
import kr.sporting.team.controller.TeamController;
import kr.sporting.team.domain.Team;
import kr.sporting.team.dto.CreateTeamRequest;
import kr.sporting.team.dto.UpdateTeamRequest;
import kr.sporting.team.repository.TeamRepository;
import kr.sporting.team.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@MockitoBean(types = {JpaMetamodelMappingContext.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(H2TestDatabaseConfig.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;
//
    @MockitoBean
    private TeamRepository teamRepository;

    private Team mockTeam;

    @BeforeEach
    void setUp() {
        teamRepository.deleteAll();
        mockTeam = Team.builder()
                .name("Mock Team")
                .address("Mock Address")
                .size(5)
                .build();
//        mockTeam.setId(1L);
    }

    @Test
    void testCreateTeam_ReturnsCreatedTeam() throws Exception {
        // given
        CreateTeamRequest request = CreateTeamRequest.builder()
                .name("New Team")
                .address("New Address")
                .size(10)
                .build();

        Mockito.when(teamService.createTeam(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(1L);
            return team;
        });

        // when & then
        mockMvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        Mockito.verify(teamService).createTeam(any(Team.class));
    }

    @Test
    void testGetTeamById_ReturnsValidTeam() throws Exception {
        // given
        Mockito.when(teamService.getTeamById(1L)).thenReturn(mockTeam);

        // when & then
        mockMvc.perform(get("/teams/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockTeam.getId()))
                .andExpect(jsonPath("$.name").value(mockTeam.getName()))
                .andExpect(jsonPath("$.address").value(mockTeam.getAddress()))
                .andExpect(jsonPath("$.size").value(mockTeam.getSize()));

        Mockito.verify(teamService).getTeamById(1L);
    }

    @Test
    void testUpdateTeam_ReturnsUpdatedTeam() throws Exception {
        // given
        UpdateTeamRequest request = UpdateTeamRequest.builder()
                .name("Updated Team")
                .address("Updated Address")
                .size(15)
                .build();

        Mockito.when(teamService.updateTeam(eq(1L), any(UpdateTeamRequest.class)))
                .thenReturn(Team.builder()
                        .id(1L)
                        .name(request.getName())
                        .address(request.getAddress())
                        .size(request.getSize())
                        .build());

        // when & then
        mockMvc.perform(put("/teams/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.address").value(request.getAddress()))
                .andExpect(jsonPath("$.size").value(request.getSize()));

        Mockito.verify(teamService).updateTeam(eq(1L), any(UpdateTeamRequest.class));
    }

    @Test
    void testDeleteTeam_ReturnsOk() throws Exception {
        // given
        Mockito.doNothing().when(teamService).deleteTeam(eq(1L));

        // when & then
        mockMvc.perform(delete("/teams/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(teamService).deleteTeam(eq(1L));
    }

    @Test
    void testGetTeams_ReturnsAllTeams() throws Exception {
        // given
        List<Team> teams = new ArrayList<>();
        teams.add(mockTeam);
        teams.add(Team.builder()
                .id(2L)
                .name("Second Team")
                .address("Second Address")
                .size(8)
                .build());

        Mockito.when(teamService.getTeams()).thenReturn(teams);

        // when & then
        mockMvc.perform(get("/teams")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(mockTeam.getId()))
                .andExpect(jsonPath("$[0].name").value(mockTeam.getName()))
                .andExpect(jsonPath("$[0].address").value(mockTeam.getAddress()))
                .andExpect(jsonPath("$[0].size").value(mockTeam.getSize()))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Second Team"))
                .andExpect(jsonPath("$[1].address").value("Second Address"))
                .andExpect(jsonPath("$[1].size").value(8));

        Mockito.verify(teamService).getTeams();
    }

    @Test
    void testCreateTeam_InvalidRequest_ReturnsBadRequest() throws Exception {
        // given: invalid request (missing required fields)
        CreateTeamRequest invalidRequest = CreateTeamRequest.builder()
                .name(null)
                .address("New Address")
                .size(0)
                .build();

        // when & then
        mockMvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTeam_InvalidRequest_ReturnsBadRequest() throws Exception {
        // given: invalid request data
        UpdateTeamRequest invalidRequest = UpdateTeamRequest.builder()
                .name("")
                .address("")
                .size(1)
                .build();

        // when & then
        mockMvc.perform(put("/teams/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}