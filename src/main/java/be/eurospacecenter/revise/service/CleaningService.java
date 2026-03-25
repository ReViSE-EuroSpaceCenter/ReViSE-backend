package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.metric.MetricType;
import be.eurospacecenter.revise.metric.RecordMetric;
import be.eurospacecenter.revise.model.GameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CleaningService {

    private static final Logger logger = LoggerFactory.getLogger(CleaningService.class);

    private final LobbyService lobbyService;
    private final List<Cleanable> cleanable;

    public CleaningService(LobbyService lobbyService, List<Cleanable> cleanable) {
        this.lobbyService = lobbyService;
        this.cleanable = cleanable;
    }

    @RecordMetric(MetricType.LOBBY_CLEARED)
    @Scheduled(cron = "0 0 */12 * * *")
    protected int clearLobbies() {
        List<String> toRemove = new ArrayList<>();

        lobbyService.lobbies.forEach((code, lobby) -> {
            GameInfo gameInfo = lobby.getGameInfo();

            if (LocalDateTime.now().isAfter(gameInfo.getExpiresAt())) {
                toRemove.add(code);
            }
        });

        cleanable.forEach(c -> c.cleanUp(toRemove));

        logger.info("Clearing {} games", toRemove.size());

        return toRemove.size();
    }

}
