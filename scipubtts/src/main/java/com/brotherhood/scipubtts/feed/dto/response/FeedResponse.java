@Builder
public record FeedResponse(
        List<FeedItemResponse> items,
        long totalItems,
){}
