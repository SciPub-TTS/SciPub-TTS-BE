@Service
public class FeedServiceImpl implements FeedService {

  @Override
  public FeedResponse getFeed(
                      FeedTab feedTab,
                      int page,
                      int pageSize) {
    return FeedResponse.builder()
            .items(List.of())
            .totalItems(0)
            .build();
  }

  @Override
  public List<FollowSummaryResponse> getFollowedTopics() {
    return List.of();
  }

  @Override
  public List<FollowSummaryResponse> getFollowedAuthors() {
    return List.of();
  }

  @Override
  public List<SuggestedTopicResponse> getSuggestedTopics() {
    return List.of();
  }
