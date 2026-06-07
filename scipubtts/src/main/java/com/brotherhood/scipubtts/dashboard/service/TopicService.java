package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicHotFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexTopicFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicCalculateResponse;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TopicService {
  private final OpenAlexService openAlexService;
  private static final long VELOCITY_PERIOD_DAYS = 14;
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
                    VELOCITY_PERIOD_DAYS,
                    ChronoUnit.DAYS
            );

    var previousEnd = currentStart;

    var previousStart =
            previousEnd.minus(
                    VELOCITY_PERIOD_DAYS,
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
    var currentEnd =
            LocalDate.parse(topic.getEndTime());

    var previousEnd =
            currentEnd.minus(
                    VELOCITY_PERIOD_DAYS,
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
                            topic.getStartTime(),
                            topic.getEndTime(),
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

    LocalDate endDate =
            LocalDate.parse(topic.getEndTime());

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
                            topic.getStartTime(),
                            topic.getEndTime(),
                            topic.getTopicId()
                    )
            );
  }

  public TopicCalculateResponse calculateAllTopicsScore(
          TopicCalculateRequest request
  ){

    var topicList = openAlexService.filterHotTopic(
            new TopicHotFilterRequest(request.fieldId())
    );

    var result = new ArrayList<Topic>();

    for (var topic : topicList.topicIdList()) {
      System.out.println("\n--------------------------------------------");
      System.out.printf("=> Processing Topic ID: %s\n", topic.getTopicId());

      // VELOCITY
      var velocity =
              calculateVelocity(
                      request.endTime(),
                      topic.getTopicId()
              );

      topic.setStartTime(
              request.startTime()
      );

      topic.setEndTime(
              request.endTime()
      );

      topic.setVelocity(
              velocity
      );

//      // ACCELERATE
//      var acceleration =
//              calculateAcceleration(
//                      topic
//              );
//
//      topic.setAcceleration(
//              acceleration
//      );
//
//      // CITATION
//      long startCitation = System.currentTimeMillis();
//      var citation =
//              calculateCitationDecay(
//                      topic
//              );
//      long endCitation = System.currentTimeMillis();
//      System.out.printf("   [Formula] calculateCitationDecay execution time: %d ms\n", (endCitation - startCitation));
//
//      topic.setCitation(citation);

      // INSTITUTION
      var institution =
              calculateInstitution(topic);

      topic.setInstitution(institution);

      result.add(
              topic
      );
    }

    return new TopicCalculateResponse(
            List.copyOf(result)
    );
  }
}