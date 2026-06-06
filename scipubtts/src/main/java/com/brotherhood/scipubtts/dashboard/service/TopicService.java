package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicHotFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexVelocityRequest;
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

  private double calculateVelocity(
          TopicCalculateRequest request, String topicId
  ){
    if(request.startTime().isEmpty() || request.endTime().isEmpty()){
      throw new BusinessException(
              ErrorCode.TOPIC_REQUEST_INVALID
      );
    }

    var currentStart =
            LocalDate.parse(request.startTime());

    var currentEnd =
            LocalDate.parse(request.endTime());

    var previousStart =
            currentStart.minus(
                    VELOCITY_PERIOD_DAYS,
                    ChronoUnit.DAYS
            );

    var previousEnd =
            currentEnd.minus(
                    VELOCITY_PERIOD_DAYS,
                    ChronoUnit.DAYS
            );

    var worksCurrentPeriod =
            openAlexService.numOfWorksInPeriodByTopic(
                    new OpenAlexVelocityRequest(
                            currentStart.toString(),
                            currentEnd.toString(),
                            topicId
                    )
            );

    var worksPreviousPeriod =
            openAlexService.numOfWorksInPeriodByTopic(
                    new OpenAlexVelocityRequest(
                            previousStart.toString(),
                            previousEnd.toString(),
                            topicId
                    )
            );

    double velocity;

    if (worksCurrentPeriod == 0) {
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

  public TopicCalculateResponse calculateVelocityAllTopics(
          TopicCalculateRequest request
  ){
    var topicList = openAlexService.filterHotTopic(
            new TopicHotFilterRequest(request.fieldId())
    );

    var result = new ArrayList<Topic>();

    for (var topic : topicList.topicIdList()) {

      var velocity =
              calculateVelocity(
                      request,
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

      result.add(
              topic
      );
    }

    return new TopicCalculateResponse(
            List.copyOf(result)
    );
  }
}