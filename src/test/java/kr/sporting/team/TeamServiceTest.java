package kr.sporting.team;

import kr.sporting.team.repository.TeamRepository;
import kr.sporting.team.domain.Team;
import kr.sporting.team.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TeamServiceTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamRepository.deleteAll();
    }

    @Test
    void testCreateTeam_SavesAndReturnsTeam() {
        // 필수 필드 포함
        Team team = new Team();
        team.setName("Celtics");
        team.setAddress("Boston");
        team.setSize(10);

        Team createdTeam = teamService.createTeam(team);

        assertThat(createdTeam.getName()).isEqualTo("Celtics");
        assertThat(createdTeam.getAddress()).isEqualTo("Boston");
    }

    @Test
    void testGetTeamById_ReturnsTeam() {
        Team team = Team.builder()
                .name("Lakers")
                .address("Los Angeles")
                .size(15)
                .build();

        Team result = teamRepository.save(team);
        Team foundTeam = teamService.getTeamById(result.getId());

        assertThat(foundTeam.getName()).isEqualTo("Lakers");
        assertThat(foundTeam.getAddress()).isEqualTo("Los Angeles");
    }

    @Test
    void testGetTeamById_ThrowsExceptionWhenNotFound() {
        assertThrows(RuntimeException.class, () -> teamService.getTeamById(1L));
    }

    @Test
    void testGetAllTeams_ReturnsAllTeams() {
        Team team1 = Team.builder()
                .name("Lakers")
                .address("Los Angeles")
                .size(15)
                .build();
        Team team2 = Team.builder()
                .name("Warriors")
                .address("San Francisco")
                .size(12)
                .build();

        teamRepository.saveAll(Arrays.asList(team1, team2));

        assertThat(teamService.getTeams()).hasSize(2)
                .extracting(Team::getName)
                .containsExactly("Lakers", "Warriors");
    }

    @Test
    void testDeleteTeam_DeletesTeam() {
        Team team1 = Team.builder()
                .name("Lakers")
                .address("Los Angeles")
                .size(15)
                .build();

        teamRepository.save(team1);

        teamService.deleteTeam(1L);

        assertThrows(RuntimeException.class, () -> teamService.getTeamById(1L));
    }
}