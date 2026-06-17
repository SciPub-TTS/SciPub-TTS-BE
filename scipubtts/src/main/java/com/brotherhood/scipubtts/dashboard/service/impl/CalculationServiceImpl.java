package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.dashboard.constant.FormulaType;
import com.brotherhood.scipubtts.dashboard.constant.weight.KeywordWeight;
import com.brotherhood.scipubtts.dashboard.constant.weight.TopicWeight;
import com.brotherhood.scipubtts.dashboard.dto.response.KeywordCalculateResponse;
import com.brotherhood.scipubtts.dashboard.dto.response.TopicScore;
import com.brotherhood.scipubtts.dashboard.entity.Keyword;
import com.brotherhood.scipubtts.dashboard.entity.Topic;
import com.brotherhood.scipubtts.dashboard.service.CalculationService;
import com.brotherhood.scipubtts.dashboard.statistic.KeywordMetricStatistic;
import com.brotherhood.scipubtts.dashboard.statistic.TopicMetricStatistic;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalculationServiceImpl implements CalculationService {

  private final Map<FormulaType, TopicWeight> topicWeights = new EnumMap<>(FormulaType.class);
  private final Map<FormulaType, KeywordWeight> keywordWeights = new EnumMap<>(FormulaType.class);

  public CalculationServiceImpl() {
    initializeTopicWeights();
    initializeKeywordWeights();
  }

  private void initializeTopicWeights() {
    topicWeights.put(FormulaType.BALANCED, new TopicWeight(0.20, 0.20, 0.20, 0.20, 0.20));
    topicWeights.put(FormulaType.TRENDING, new TopicWeight(0.35, 0.30, 0.20, 0.10, 0.05));
    topicWeights.put(FormulaType.EMERGING, new TopicWeight(0.15, 0.10, 0.15, 0.35, 0.25));
    topicWeights.put(FormulaType.IMPACT,   new TopicWeight(0.15, 0.10, 0.50, 0.10, 0.15));
  }

  private void initializeKeywordWeights() {
    keywordWeights.put(FormulaType.BALANCED, new KeywordWeight(0.33, 0.33, 0.34));
    keywordWeights.put(FormulaType.TRENDING, new KeywordWeight(0.45, 0.40, 0.15));
    keywordWeights.put(FormulaType.EMERGING, new KeywordWeight(0.50, 0.45, 0.05));
    keywordWeights.put(FormulaType.DOMINANT, new KeywordWeight(0.20, 0.20, 0.60));
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private TopicWeight resolveTopicWeight(FormulaType formulaType) {
    TopicWeight weight = topicWeights.get(formulaType);
    if (weight == null) {
      throw new IllegalArgumentException(
              "No TopicWeight configured for formula: " + formulaType);
    }
    return weight;
  }

  private KeywordWeight resolveKeywordWeight(FormulaType formulaType) {
    KeywordWeight weight = keywordWeights.get(formulaType);
    if (weight == null) {
      throw new IllegalArgumentException(
              "No KeywordWeight configured for formula: " + formulaType);
    }
    return weight;
  }

  private double normalize(double value, double min, double max) {
    if (Double.compare(max, min) == 0) return 0D;
    return (value - min) / (max - min);
  }

  @Override
  public double toNormalizedPercent(double value, double min, double max) {
    return Math.round(normalize(value, min, max) * 10000.0) / 100.0;
  }

  // ── statistic builders ───────────────────────────────────────────────────────

  @Override
  public TopicMetricStatistic buildTopicMetricStatistic(List<Topic> topics) {
    return buildTopicStatistic(topics);
  }

  private TopicMetricStatistic buildTopicStatistic(List<Topic> topics) {
    double velocityMin = Double.MAX_VALUE,    velocityMax = -Double.MAX_VALUE;
    double accelerationMin = Double.MAX_VALUE, accelerationMax = -Double.MAX_VALUE;
    double citationMin = Double.MAX_VALUE,    citationMax = -Double.MAX_VALUE;
    double newcomerMin = Double.MAX_VALUE,    newcomerMax = -Double.MAX_VALUE;
    double institutionMin = Double.MAX_VALUE, institutionMax = -Double.MAX_VALUE;

    for (Topic t : topics) {
      velocityMin     = Math.min(velocityMin,     t.getVelocity());
      velocityMax     = Math.max(velocityMax,     t.getVelocity());
      accelerationMin = Math.min(accelerationMin, t.getAcceleration());
      accelerationMax = Math.max(accelerationMax, t.getAcceleration());
      citationMin     = Math.min(citationMin,     t.getCitationDecay());
      citationMax     = Math.max(citationMax,     t.getCitationDecay());
      newcomerMin     = Math.min(newcomerMin,     t.getNewComerAuthor());
      newcomerMax     = Math.max(newcomerMax,     t.getNewComerAuthor());
      institutionMin  = Math.min(institutionMin,  t.getInstitution());
      institutionMax  = Math.max(institutionMax,  t.getInstitution());
    }

    return new TopicMetricStatistic(
            velocityMin, velocityMax,
            accelerationMin, accelerationMax,
            citationMin, citationMax,
            newcomerMin, newcomerMax,
            institutionMin, institutionMax
    );
  }

  private KeywordMetricStatistic buildKeywordStatistic(List<Keyword> keywords) {
    double pgrMin = Double.MAX_VALUE,  pgrMax = -Double.MAX_VALUE;
    double cagrMin = Double.MAX_VALUE, cagrMax = -Double.MAX_VALUE;
    double psMin = Double.MAX_VALUE,   psMax = -Double.MAX_VALUE;

    for (Keyword k : keywords) {
      pgrMin  = Math.min(pgrMin,  k.getPgr());
      pgrMax  = Math.max(pgrMax,  k.getPgr());
      cagrMin = Math.min(cagrMin, k.getCagr());
      cagrMax = Math.max(cagrMax, k.getCagr());
      psMin   = Math.min(psMin,   k.getPs());
      psMax   = Math.max(psMax,   k.getPs());
    }

    return new KeywordMetricStatistic(pgrMin, pgrMax, cagrMin, cagrMax, psMin, psMax);
  }

  // ── score calculators (stateless, take pre-built stat) ──────────────────────

    double calculateTopicScore(
          Topic topic, TopicWeight weight, TopicMetricStatistic stat) {
    return normalize(topic.getVelocity(),      stat.velocityMin(),     stat.velocityMax())     * weight.velocity()
            + normalize(topic.getAcceleration(),  stat.accelerationMin(), stat.accelerationMax()) * weight.acceleration()
            + normalize(topic.getCitationDecay(), stat.citationMin(),     stat.citationMax())     * weight.citationDecay()
            + normalize(topic.getNewComerAuthor(),stat.newcomerMin(),     stat.newcomerMax())     * weight.newcomerAuthor()
            + normalize(topic.getInstitution(),   stat.institutionMin(),  stat.institutionMax())  * weight.institution();
  }

  private double calculateKeywordScore(
          Keyword keyword, KeywordWeight weight, KeywordMetricStatistic stat) {
    return normalize(keyword.getPgr(),  stat.pgrMin(),  stat.pgrMax())  * weight.pgr()
            + normalize(keyword.getCagr(), stat.cagrMin(), stat.cagrMax()) * weight.cagr()
            + normalize(keyword.getPs(),   stat.psMin(),   stat.psMax())   * weight.ps();
  }

  // ── public API ───────────────────────────────────────────────────────────────

  @Override
  public List<TopicScore> calculateTopicsFinalScore(String formula, List<Topic> topicList) {
    FormulaType formulaType = FormulaType.from(formula);
    TopicWeight weight = resolveTopicWeight(formulaType);
    TopicMetricStatistic stat = buildTopicStatistic(topicList);

    Map<String, Double> scoreMap = new HashMap<>();
    for (Topic topic : topicList) {
      scoreMap.put(topic.getTopicId(), calculateTopicScore(topic, weight, stat));
    }

    return topicList.stream()
            .map(topic -> new TopicScore(topic, calculateTopicScore(topic, weight, stat)))
            .sorted(Comparator.comparing(TopicScore::score).reversed())
            .limit(10)
            .toList();
  }

  @Override
  public double calculateTopicFinalScore(String formula, String topicId, List<Topic> topicList) {
    FormulaType formulaType = FormulaType.from(formula);
    TopicWeight weight = resolveTopicWeight(formulaType);
    TopicMetricStatistic stat = buildTopicStatistic(topicList);

    Topic topic = topicList.stream()
            .filter(t -> t.getTopicId().equals(topicId))
            .findFirst()
            .orElse(null);
    if (topic == null) return 0D;

    return calculateTopicScore(topic, weight, stat);
  }

  @Override
  public KeywordCalculateResponse calculateKeywordsFinalScore(String formula, List<Keyword> keywordList) {
    keywordList = keywordList.stream()
            .filter(k ->
                    k.getPgr() != null
                            && k.getCagr() != null
                            && k.getPs() != null)
            .toList();

    if (keywordList.isEmpty()) {
      return new KeywordCalculateResponse(List.of());
    }

    FormulaType formulaType = FormulaType.from(formula);
    KeywordWeight weight = resolveKeywordWeight(formulaType);
    KeywordMetricStatistic stat = buildKeywordStatistic(keywordList);

    Map<String, Double> scoreMap = new HashMap<>();
    for (Keyword keyword : keywordList) {
      scoreMap.put(keyword.getKeywordId(), calculateKeywordScore(keyword, weight, stat));
    }

    Map<String, Keyword> keywordById = keywordList.stream()
            .collect(Collectors.toMap(Keyword::getKeywordId, k -> k));

    List<Keyword> top10 = scoreMap.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(10)
            .map(e -> keywordById.get(e.getKey()))
            .toList();

    return new KeywordCalculateResponse(top10);
  }

  @Override
  public double calculateKeywordFinalScore(String formula, String keywordId, List<Keyword> keywordList) {
    FormulaType formulaType = FormulaType.from(formula);
    KeywordWeight weight = resolveKeywordWeight(formulaType);
    KeywordMetricStatistic stat = buildKeywordStatistic(keywordList);

    Keyword keyword = keywordList.stream()
            .filter(k -> k.getKeywordId().equals(keywordId))
            .findFirst()
            .orElse(null);
    if (keyword == null) return 0D;

    return calculateKeywordScore(keyword, weight, stat);
  }
}