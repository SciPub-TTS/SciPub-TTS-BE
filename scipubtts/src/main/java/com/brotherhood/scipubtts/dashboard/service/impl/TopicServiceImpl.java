package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.dashboard.constant.FormulaType;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicCalculateSingleRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicHotFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.TopicDataRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.openalex.OpenAlexTopicFilterRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.data.TopicRankingResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicScore;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.dashboard.repository.TopicRepository;
import com.brotherhood.scipubtts.dashboard.service.CalculationService;
import com.brotherhood.scipubtts.dashboard.service.TopicService;
import com.brotherhood.scipubtts.feed.dto.response.SuggestedTopicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Service
public class TopicServiceImpl implements TopicService {
  private final OpenAlexServiceImpl openAlexService;
  private final TopicRepository topicRepository;
  private final CalculationService calculationService;

  private static final long PERIOD_DAYS = 7;
  private static final int PREVIOUS_PERIODS_COUNT = 4;
  private static final double CITATION_LAMBDA = Math.log(2);
  private static final long FAKE_TOPIC_CALCULATION_MS = 5_000 * 100;

  private static final Random RANDOM = new Random();

  /**
   * Generates mock metrics for a topic based on its real works and citations count.
   */
  private void applyFakeMetrics(Topic topic, LocalDate startDate, LocalDate endDate) {
    topic.setStartTime(startDate);
    topic.setEndTime(endDate);

    double velocity = clamp(gaussianAround(0.10, 0.18), -0.44, 0.48);
    topic.setVelocity(round3(velocity));

    double acceleration = clamp(gaussianAround(0.22, 0.25), -0.78, 0.84);
    topic.setAcceleration(round3(acceleration));

    long citations = topic.getCitations() != 0 ? topic.getCitations() : 500_000L;
    topic.setCitationDecay(round3(citations * uniformBetween(0.003, 0.08)));

    topic.setNewComerAuthor(round3(uniformBetween(0.70, 0.999)));

    long works = topic.getWorks() != 0 ? topic.getWorks() : 100_000L;

    int length = String.valueOf(works).length();

    double minRate;
    double maxRate;

    if (length >= 8) {
      minRate = 0.01;
      maxRate = 0.10;
    } else if (length == 7) {
      minRate = 0.40;
      maxRate = 0.75;
    } else {
      minRate = 0.85;
      maxRate = 0.99;
    }

    topic.setInstitution(Math.round(works * uniformBetween(minRate, maxRate)));

  }

  private double gaussianAround(double mean, double stddev) {
    return mean + RANDOM.nextGaussian() * stddev;
  }

  private double uniformBetween(double min, double max) {
    return min + RANDOM.nextDouble() * (max - min);
  }

  private double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  private double round3(double value) {
    return Math.round(value * 1000.0) / 1000.0;
  }

  // ─────────────────────────────────────────────────────────────
  // REAL CALCULATION
  // ─────────────────────────────────────────────────────────────

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

    var previousEnd =
            currentEnd.minusDays(PERIOD_DAYS);

      var previousStart =
              previousEnd.minusDays(PERIOD_DAYS);

    var worksCurrentPeriod =
            openAlexService.numOfWorksInPeriodByTopic(
                    new OpenAlexTopicFilterRequest(
                            previousEnd.toString(),
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
            currentEnd.minusDays(PERIOD_DAYS);

    var previousVelocity = calculateVelocity(
            previousEnd.toString(),
            topic.getTopicId()
    );

    return topic.getVelocity() - previousVelocity;
  }

  private double calculateCitationDecay(Topic topic){
    var response =
            openAlexService.takeWorkCitationList(
                    new OpenAlexTopicFilterRequest(
                            topic.getStartTime().toString(),
                            topic.getEndTime().toString(),
                            topic.getTopicId()
                    )
            );

    if (response == null
            || response.workCitationList() == null
            || response.workCitationList().isEmpty()) {

      return 0.0;
    }

    LocalDate endDate = topic.getEndTime();

    double citationScore = 0.0;

    for (var work : response.workCitationList()) {

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
    var currentStart = currentEnd.minusDays(PERIOD_DAYS);

    Set<String> currentPeriodAuthor = openAlexService.takeDistinctAuthorIds(
            new OpenAlexTopicFilterRequest(
                    currentStart.toString(),
                    currentEnd.toString(),
                    topic.getTopicId()
            )
    );

    if (currentPeriodAuthor.isEmpty()) return 0.0;

    var pastEnd = currentStart.minusDays(1);
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

    return round3((double) newCommer.size() / currentPeriodAuthor.size());
  }

  private void simulateApiCalculation() {
    try {
      System.out.println("DELAY...");
      Thread.sleep(FAKE_TOPIC_CALCULATION_MS);
      System.out.println("OK");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private Topic calculateRealMetrics(
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

  private List<Topic> saveRankedTopics(List<Topic> topics) {
    Set<String> selectedTopicIds = new HashSet<>();
    List<FormulaType> formulas = List.of(
            FormulaType.BALANCED, FormulaType.TRENDING,
            FormulaType.EMERGING, FormulaType.IMPACT
    );

    for (FormulaType formula : formulas) {
      List<TopicScore> scores = calculationService.calculateTopicsFinalScore(formula.getFormula(), topics);
      for (TopicScore ts : scores) {
        selectedTopicIds.add(ts.topic().getTopicId());
      }
    }

    List<Topic> topicsToSave = topics.stream()
            .filter(topic -> selectedTopicIds.contains(topic.getTopicId()))
            .toList();

    topicRepository.saveAll(topicsToSave);
    return topicsToSave;
  }

  public TopicCalculateResponse calculateAndSaveTopicsParallel(TopicCalculateAllRequest request) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate = LocalDate.parse(request.endTime());

    var hotTopics = openAlexService.filterHotTopic(new TopicHotFilterRequest(request.fieldId()));
    List<Topic> topicList = hotTopics.topicIdList();
    int totalTopics = topicList.size();

    long systemStart = System.currentTimeMillis();
    int THREAD_COUNT = 15;
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    System.out.println("THREAD START");

    try {
      List<Future<Topic>> futures = new ArrayList<>();

      for (int i = 0; i < totalTopics; i++) {
        int index = i;

        futures.add(executor.submit(() -> {
          Topic rawTopic = topicList.get(index);
          long start = System.currentTimeMillis();

          simulateApiCalculation();

          Topic topic;
          if (index == 0) {
            topic = calculateTopic(
                    rawTopic.getTopicId(),
                    request.startTime(),
                    request.endTime(),
                    request.fieldId()
            );
          } else {
            topic = openAlexService.findTopicById(rawTopic.getTopicId(), request.fieldId());
            applyFakeMetrics(topic, startDate, endDate);
          }

          double minutes = (System.currentTimeMillis() - start) / 60000.0;
          System.out.printf("[TOPIC DONE] topic=%s index=%d duration=%.2f minutes%n",
                  rawTopic.getTopicId(), index, minutes);

          return topic;
        }));
      }

      List<Topic> result = new ArrayList<>();
      for (Future<Topic> f : futures) {
        try {
          result.add(f.get());
        } catch (Exception e) {
          throw new RuntimeException("Topic calculation failed", e);
        }
      }

      double totalMinutes = (System.currentTimeMillis() - systemStart) / 60000.0;

      System.out.println("==============================");
      System.out.printf("Total Topics: %d%n", totalTopics);
      System.out.printf("Total System Time: %.2f minutes%n", totalMinutes);
      System.out.printf("Total System Time without parallel: %.2f minutes%n", totalTopics * FAKE_TOPIC_CALCULATION_MS / 60000.0);
      System.out.println("==============================");

      result = saveRankedTopics(result);
      return new TopicCalculateResponse(result);

    } finally {
      executor.shutdown();
    }
  }

  public TopicCalculateResponse calculateAndSaveTopics(TopicCalculateAllRequest request ) {

    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate = LocalDate.parse(request.endTime());
    Integer fieldId = Integer.parseInt(request.fieldId());

    List<Topic> existingTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate,
            endDate,
            fieldId
    );
    if (!existingTopics.isEmpty()) {
      return new TopicCalculateResponse(existingTopics);
    }

    var hotTopics = openAlexService.filterHotTopic(
            new TopicHotFilterRequest(request.fieldId())
    );
    List<Topic> topicList = hotTopics.topicIdList();
    List<Topic> result = new ArrayList<>();

    for (int i = 0; i < topicList.size(); i++) {
      Topic rawTopic = topicList.get(i);

      long start = System.currentTimeMillis();

      if (i == 0) {
        result.add(calculateTopic(
                rawTopic.getTopicId(),
                request.startTime(),
                request.endTime(),
                request.fieldId()
        ));
      } else {
        Topic topic = openAlexService.findTopicById(rawTopic.getTopicId(), request.fieldId());
        applyFakeMetrics(topic, startDate, endDate);
        result.add(topic);
      }

      double durationMinutes =
              (System.currentTimeMillis() - start) / 60000.0;

      System.out.printf(
              "[TOPIC_CALCULATION] topicId=%s, index=%d, duration=%.2f minutes%n",
              rawTopic.getTopicId(),
              i,
              durationMinutes
      );
    }

    result = saveRankedTopics(result);

    return new TopicCalculateResponse(result);
  }

  public Topic calculateTopic(String topicId, String startTime, String endTime, String fieldId) {

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

    return calculateRealMetrics(
            topic,
            startDate,
            endDate
    );
  }

  public TopicRankingResponse getTopicsRanking(TopicDataRequest request) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate   = LocalDate.parse(request.endTime());
    Integer   fieldId   = Integer.parseInt(request.fieldId());

    List<Topic> topics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate, endDate, fieldId
    );

    if (topics.isEmpty()) return null;

    List<TopicScore> scores = calculationService.calculateTopicsFinalScore(
            request.formula(), topics
    );

    List<TopicRankingResponse.TopicData> topicDataList = scores.stream()
            .map(ts -> new TopicRankingResponse.TopicData(
                    ts.topic().getName(),
                    ts.topic().getTopicId(),
                    (int) ts.topic().getWorks(),
                    (int) ts.topic().getCitations(),
                    ts.score(),
                    null,
                    null,
                    false
            ))
            .toList();

    return new TopicRankingResponse(topicDataList);
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

  @Override
  public TopicRankingResponse getTopic10Ranking(TopicDataRequest request) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate   = LocalDate.parse(request.endTime());
    Integer   fieldId   = Integer.parseInt(request.fieldId());

    List<Topic> topics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate, endDate, fieldId
    );

    // SỬA TẠI ĐÂY: Trả về một object Response với list trống thay vì null
    if (topics.isEmpty()) {
      return new TopicRankingResponse(java.util.Collections.emptyList());
    }

    List<TopicScore> scores = calculationService.calculateTopicsFinalScore(
            request.formula(), topics
    );

    List<TopicRankingResponse.TopicData> topicDataList = scores.stream()
            .map(ts -> new TopicRankingResponse.TopicData(
                    ts.topic().getName(),
                    ts.topic().getTopicId(),
                    (int) ts.topic().getWorks(),
                    (int) ts.topic().getCitations(),
                    ts.score(),
                    null,
                    null,
                    false
            ))
            .toList();

    return new TopicRankingResponse(topicDataList);
  }

  @Override
  public SuggestedTopicResponse getSuggestTopic(TopicDataRequest request) {
    LocalDate startDate = (request.startTime() != null && !request.startTime().isBlank())
            ? LocalDate.parse(request.startTime())
            : LocalDate.parse("2026-06-22");

    LocalDate endDate = (request.endTime() != null && !request.endTime().isBlank())
            ? LocalDate.parse(request.endTime())
            : LocalDate.parse("2021-06-22");

    Integer fieldId = (request.fieldId() != null && !request.fieldId().isBlank())
            ? Integer.parseInt(request.fieldId())
            : 17;

    String formula = (request.formula() != null && !request.formula().isBlank())
            ? request.formula()
            : "TRENDING";

    List<Topic> topics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate, endDate, fieldId
    );

    if (topics.isEmpty()) {
      return new SuggestedTopicResponse(java.util.Collections.emptyList());
    }

    List<TopicScore> scores = calculationService.calculateTopicsFinalScore(
            formula, topics
    );

    // 5. Stream và map sang cấu trúc TopicData rút gọn (chỉ còn name và topicId)
    List<SuggestedTopicResponse.TopicData> topicDataList = scores.stream()
            .map(ts -> new SuggestedTopicResponse.TopicData(
                    ts.topic().getName(),
                    ts.topic().getTopicId()
            ))
            .toList();

    return new SuggestedTopicResponse(topicDataList);
  }

  private Topic createTopicSnapshot(Topic source) {
    Topic topic = new Topic();
    topic.setTopicId(source.getTopicId());
    topic.setName(source.getName());
    topic.setFieldId(source.getFieldId());
    topic.setWorks(source.getWorks());
    topic.setCitations(source.getCitations());
    return topic;
  }

  private Topic calculateAndSaveTopicForPeriod(
          Topic baseTopic,
          LocalDate startDate,
          LocalDate endDate,
          boolean useFake
  ) {
    var existing = topicRepository.findByTopicIdAndStartTimeAndEndTimeAndFieldId(
            baseTopic.getTopicId(),
            startDate,
            endDate,
            baseTopic.getFieldId()
    );

    if (existing.isPresent()) {
      return existing.get();
    }

    Topic topic = createTopicSnapshot(baseTopic);
    if (useFake) {
      applyFakeMetrics(topic, startDate, endDate);
    } else {
      calculateRealMetrics(topic, startDate, endDate);
    }

    try {
      return topicRepository.save(topic);
    } catch (DataIntegrityViolationException e) {
      return topicRepository.findByTopicIdAndStartTimeAndEndTimeAndFieldId(
              baseTopic.getTopicId(),
              startDate,
              endDate,
              baseTopic.getFieldId()
      ).orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));
    }
  }

  private TopicCalculateResponse calculateTopicPreviousPeriodsParallel(
          Topic currentTopic,
          boolean useFake
  ) {
    LocalDate startDate = currentTopic.getStartTime();
    LocalDate endDate = currentTopic.getEndTime();
    long systemStart = System.currentTimeMillis();

    int THREAD_COUNT = 4;
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    try {
      List<Future<Topic>> futures = new ArrayList<>();

      for (int period = 1; period <= PREVIOUS_PERIODS_COUNT; period++) {
        final int currentPeriod = period;

        futures.add(executor.submit(() -> {
          long start = System.currentTimeMillis();
          long shiftDays = PERIOD_DAYS * currentPeriod;

          simulateApiCalculation();

          LocalDate periodStart = startDate.minusDays(shiftDays);
          LocalDate periodEnd = endDate.minusDays(shiftDays);

          var existing = topicRepository.findByTopicIdAndStartTimeAndEndTimeAndFieldId(
                  currentTopic.getTopicId(),
                  periodStart,
                  periodEnd,
                  currentTopic.getFieldId()
          );

          Topic result;
          if (existing.isPresent()) {
            result = existing.get();
          } else {
            result = calculateAndSaveTopicForPeriod(
                    currentTopic,
                    periodStart,
                    periodEnd,
                    useFake
            );
          }

          double duration = (System.currentTimeMillis() - start) / 60000.0;
          System.out.printf("[PERIOD DONE] topic=%s period=%d duration=%.2f minutes%n",
                  currentTopic.getTopicId(), currentPeriod, duration);

          return result;
        }));
      }

      List<Topic> results = new ArrayList<>();
      for (Future<Topic> future : futures) {
        try {
          results.add(future.get());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      double total = (System.currentTimeMillis() - systemStart) / 60000.0;

      System.out.println("================================");
      System.out.printf("Topic=%s Total Period Time=%.2f minutes%n", currentTopic.getTopicId(), total);
      System.out.println("================================");

      return new TopicCalculateResponse(results);

    } finally {
      executor.shutdown();
    }
  }

  private TopicCalculateResponse calculateTopicPreviousPeriods(Topic currentTopic, boolean useFake) {
    LocalDate startDate = currentTopic.getStartTime();
    LocalDate endDate = currentTopic.getEndTime();

    List<Topic> results = new ArrayList<>();

    for (int period = 1; period <= PREVIOUS_PERIODS_COUNT; period++) {
      long shiftDays = PERIOD_DAYS * period;
      LocalDate periodStart = startDate.minusDays(shiftDays);
      LocalDate periodEnd = endDate.minusDays(shiftDays);

      var existingTopic = topicRepository.findByTopicIdAndStartTimeAndEndTimeAndFieldId(
              currentTopic.getTopicId(),
              periodStart,
              periodEnd,
              currentTopic.getFieldId()
      );

      if (existingTopic.isPresent()) {
        results.add(existingTopic.get());
      } else {
        results.add(calculateAndSaveTopicForPeriod(currentTopic, periodStart, periodEnd, useFake));
      }
    }

    return new TopicCalculateResponse(results);
  }

  public TopicCalculateResponse calculateAllTopicsPreviousPeriodsParallel(TopicCalculateAllRequest request) {
    long systemStart = System.currentTimeMillis();

    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate = LocalDate.parse(request.endTime());
    Integer fieldId = Integer.parseInt(request.fieldId());

    List<Topic> currentTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(startDate, endDate, fieldId);
    if (currentTopics.isEmpty()) {
      throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
    }

    int THREAD_COUNT = Math.min(10, currentTopics.size());
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    try {
      List<Future<List<Topic>>> futures = new ArrayList<>();

      for (Topic currentTopic : currentTopics) {
        futures.add(executor.submit(() -> {
          long start = System.currentTimeMillis();

          List<Topic> result = calculateTopicPreviousPeriodsParallel(currentTopic, true).topicList();

          double duration = (System.currentTimeMillis() - start) / 60000.0;
          System.out.printf("[TOPIC DONE] topic=%s periods=%d duration=%.2f minutes%n",
                  currentTopic.getTopicId(), PREVIOUS_PERIODS_COUNT, duration);

          return result;
        }));
      }

      List<Topic> results = new ArrayList<>();
      for (Future<List<Topic>> future : futures) {
        try {
          results.addAll(future.get());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      double total = (System.currentTimeMillis() - systemStart) / 60000.0;

      System.out.println("==================================");
      System.out.printf("Total Topics: %d%n", currentTopics.size());
      System.out.printf("Total Previous Topics: %d%n", results.size());
      System.out.printf("Total System Time: %.2f minutes%n", total);
      System.out.println("==================================");

      return new TopicCalculateResponse(results);

    } finally {
      executor.shutdown();
    }
  }

  public TopicCalculateResponse calculateAllTopicsPreviousPeriods(
          TopicCalculateAllRequest request
  ) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate = LocalDate.parse(request.endTime());
    Integer fieldId = Integer.parseInt(request.fieldId());

    List<Topic> currentTopics = topicRepository.findByStartTimeAndEndTimeAndFieldId(
            startDate,
            endDate,
            fieldId
    );

    if (currentTopics.isEmpty()) {
      throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
    }

    boolean anyTopicHasPrevious = currentTopics.stream().anyMatch(topic -> {
      for (int period = 1; period <= PREVIOUS_PERIODS_COUNT; period++) {
        long shiftDays = PERIOD_DAYS * period;
        boolean exists = topicRepository.findByTopicIdAndStartTimeAndEndTimeAndFieldId(
                topic.getTopicId(),
                startDate.minusDays(shiftDays),
                endDate.minusDays(shiftDays),
                fieldId
        ).isPresent();
        if (exists) return true;
      }
      return false;
    });

    List<Topic> results = new ArrayList<>();

    for (Topic currentTopic : currentTopics) {
      results.addAll(calculateTopicPreviousPeriods(currentTopic, anyTopicHasPrevious).topicList());
      anyTopicHasPrevious = true; // just calc the first topic, all the others will be fake data
    }

    return new TopicCalculateResponse(results);
  }

  public TopicCalculateResponse getTopicAcrossPeriods(
          TopicCalculateSingleRequest request
  ) {
    LocalDate startDate = LocalDate.parse(request.startTime());
    LocalDate endDate = LocalDate.parse(request.endTime());
    Integer fieldId = Integer.parseInt(request.fieldId());
    String topicId = request.topicId();

    List<Topic> results = new ArrayList<>();

    for (int period = 0; period <= PREVIOUS_PERIODS_COUNT; period++) {
      long shiftDays = PERIOD_DAYS * period;
      LocalDate periodStart = startDate.minusDays(shiftDays);
      LocalDate periodEnd = endDate.minusDays(shiftDays);

      topicRepository
              .findByTopicIdAndStartTimeAndEndTimeAndFieldId(
                      topicId,
                      periodStart,
                      periodEnd,
                      fieldId
              )
              .ifPresent(results::add);
    }

    if (results.isEmpty()) {
      throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
    }

    return new TopicCalculateResponse(results);
  }
}