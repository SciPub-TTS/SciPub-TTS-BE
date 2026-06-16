@Builder
public record SuggestedTopicResponse(
        Long id,
        String topicName,
        Double trendScore
){}
