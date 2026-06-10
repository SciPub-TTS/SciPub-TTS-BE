package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicHotFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexTopicFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicCalculateResponse;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.dashboard.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TopicService {
  private final OpenAlexService openAlexService;
  private final TopicRepository topicRepository;

  private static final long PERIOD_DAYS = 14;
  private static final double CITATION_LAMBDA = Math.log(2);

  private double calculateVelocity(
          String endTime, String topicId
  ){
    if(endTime.isEmpty()){
      throw new BusinessException(
              ErrorCode.TOPIC_REQUEST_INVALID
      );
    }

    var currentEnd =
            LocalDate.parse(endTime);

    var currentStart =
            currentEnd.minus(
                    PERIOD_DAYS,
                    ChronoUnit.DAYS
            );

    var previousEnd = currentStart;

    var previousStart =
            previousEnd.minus(
                    PERIOD_DAYS,
                    ChronoUnit.DAYS
            );

    var worksCurrentPeriod =
            openAlexService.numOfWorksInPeriodByTopic(
                    new OpenAlexTopicFilterRequest(
                            currentStart.toString(),
                            currentEnd.toString(),
                            topicId
                    )
            );

    var worksPreviousPeriod =
            openAlexService.numOfWorksInPeriodByTopic(
                    new OpenAlexTopicFilterRequest(
                            previousStart.toString(),
                            previousEnd.toString(),
                            topicId
                    )
            );

    double velocity;

    if (worksPreviousPeriod == 0) {
      velocity =
              worksCurrentPeriod > 0
                      ? 1.0
                      : 0.0;
    } else {
      velocity =
              (double)
                      (worksCurrentPeriod - worksPreviousPeriod)
                      / worksPreviousPeriod;
    }

    return velocity;
  }

  private double calculateAcceleration(Topic topic){
    var currentEnd = topic.getEndTime();

    var previousEnd =
            currentEnd.minus(
                    PERIOD_DAYS,
                    ChronoUnit.DAYS
            );

    var previousVelocity = calculateVelocity(
            previousEnd.toString(),
            topic.getTopicId()
    );

    return topic.getVelocity() - previousVelocity;
  }

  private double calculateCitationDecay(Topic topic){

    long totalStart = System.nanoTime();
    System.out.println("[CitationDecay] START - topicId=" + topic.getTopicId());

    long apiStart = System.nanoTime();
    var response =
            openAlexService.takeWorkCitationList(
                    new OpenAlexTopicFilterRequest(
                            topic.getStartTime().toString(),
                            topic.getEndTime().toString(),
                            topic.getTopicId()
                    )
            );
    long apiEnd = System.nanoTime();
    System.out.println(
            "[CitationDecay] API completed in "
                    + ((apiEnd - apiStart) / 1_000_000.0)
                    + " ms"
    );

    if (response == null
            || response.workCitationList() == null
            || response.workCitationList().isEmpty()) {

      System.out.println("[CitationDecay] Empty response");
      System.out.println(
              "[CitationDecay] TOTAL execution time = "
                      + ((System.nanoTime() - totalStart) / 1_000_000.0)
                      + " ms"
      );

      return 0.0;
    }

    // PREPARE
    long prepareStart = System.nanoTime();

    LocalDate endDate = topic.getEndTime();

    long prepareEnd = System.nanoTime();
    System.out.println(
            "[CitationDecay] Prepare phase took "
                    + ((prepareEnd - prepareStart) / 1_000_000.0)
                    + " ms"
    );

    // CALCULATE
    long calcStart = System.nanoTime();
    int workCount = 0;

    double citationScore = 0.0;

    for (var work : response.workCitationList()) {

      workCount++;

      double citationCount = work.citedByCount();

      LocalDate publicationDate =
              LocalDate.parse(work.publicationDate());

      long daysBetween =
              ChronoUnit.DAYS.between(
                      publicationDate,
                      endDate
              );

      double age =
              daysBetween / 365.25;

      citationScore +=
              citationCount
                      * Math.exp(
                      -CITATION_LAMBDA * age
              );
    }
    long calcEnd = System.nanoTime();
    System.out.println(
            "[CitationDecay] Calculation completed for "
                    + workCount
                    + " works in "
                    + ((calcEnd - calcStart) / 1_000_000.0)
                    + " ms"
    );

    return Math.round(citationScore * 1000.0) / 1000.0;
  }

  private double calculateInstitution(
          Topic topic
  ) {

    return openAlexService
            .countInstitutionByTopic(
                    new OpenAlexTopicFilterRequest(
                            topic.getStartTime().toString(),
                            topic.getEndTime().toString(),
                            topic.getTopicId()
                    )
            );
  }

  private double calculateNewcomerRatio(Topic topic){
    var currentEnd = topic.getEndTime();
    var currentStart = currentEnd.minus(PERIOD_DAYS, ChronoUnit.DAYS);

    Set<String> currentPeriodAuthor = openAlexService.takeDistinctAuthorIds(
            new OpenAlexTopicFilterRequest(
                    currentStart.toString(),
                    currentEnd.toString(),
                    topic.getTopicId()
            )
    );

    if (currentPeriodAuthor.isEmpty()) return 0.0;

    var pastEnd = currentStart.minus(1, ChronoUnit.DAYS);
    var pastStart = topic.getStartTime();
    Set<String> allAuthor = openAlexService.takeDistinctAuthorIds(
            new OpenAlexTopicFilterRequest(
                    pastStart.toString(),
                    pastEnd.toString(),
                    topic.getTopicId()
            )
    );

    Set<String> newCommer = new HashSet<>(currentPeriodAuthor);
    newCommer.removeAll(allAuthor);

    double ratio = (double) newCommer.size() / currentPeriodAuthor.size();

    return Math.round(ratio * 1000.0) / 1000.0;
  }

  private Topic calculateTopic(
          Topic topic,
          LocalDate startDate,
          LocalDate endDate
  ) {

    topic.setStartTime(startDate);
    topic.setEndTime(endDate);

    double velocity = calculateVelocity(
            endDate.toString(),
            topic.getTopicId()
    );

    topic.setVelocity(velocity);

    double acceleration = calculateAcceleration(topic);
    topic.setAcceleration(acceleration);

    double citationDecay = calculateCitationDecay(topic);
    topic.setCitationDecay(citationDecay);

    double institution = calculateInstitution(topic);
    topic.setInstitution(institution);

    double newcomerRatio = calculateNewcomerRatio(topic);
    topic.setNewComerAuthor(newcomerRatio);

    return topic;
  }

  public TopicCalculateResponse calculateAndSaveTopics(TopicCalculateAllRequest request ) {

    var hotTopics = openAlexService.filterHotTopic(
            new TopicHotFilterRequest(request.fieldId())
    );

    List<Topic> result = new ArrayList<>();

    for (Topic topic : hotTopics.topicIdList()) {
      result.add(
              calculateAndSaveTopic(
                      topic.getTopicId(),
                      request.startTime(),
                      request.endTime(),
                      request.fieldId()
              )
      );
    }

    return new TopicCalculateResponse(result);
  }

  public Topic calculateAndSaveTopic(String topicId, String startTime, String endTime, String fieldId) {

    LocalDate startDate = LocalDate.parse(startTime);
    LocalDate endDate = LocalDate.parse(endTime);

    var existingTopic = topicRepository.findByTopicIdAndStartTimeAndEndTime(
            topicId,
            startDate,
            endDate
    );

    if (existingTopic.isPresent()) {
      return existingTopic.get();
    }

    Topic topic = openAlexService.findTopicById(topicId, fieldId);

    Topic calculatedTopic = calculateTopic(
            topic,
            startDate,
            endDate
    );

    return topicRepository.save(calculatedTopic);
  }

  public TopicCalculateResponse getTopicsFromDb(
          TopicCalculateAllRequest request
  ) {

    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate = LocalDate.parse(request.endTime());
    Integer fieldId = Integer.parseInt(request.fieldId());

    List<Topic> topics =
            topicRepository.findByStartTimeAndEndTimeAndFieldId(
                    startDate,
                    endDate,
                    fieldId
            );

    if (topics.isEmpty()) {
      return null;
    }

    return new TopicCalculateResponse(topics);
  }

  public Topic getTopicFromDb(
          String topicId,
          String startTime,
          String endTime
  ) {

    return topicRepository
            .findByTopicIdAndStartTimeAndEndTime(
                    topicId,
                    LocalDate.parse(startTime),
                    LocalDate.parse(endTime)
            )
            .orElseThrow(() -> new BusinessException(
                    ErrorCode.TOPIC_NOT_FOUND
            ));
  }
}