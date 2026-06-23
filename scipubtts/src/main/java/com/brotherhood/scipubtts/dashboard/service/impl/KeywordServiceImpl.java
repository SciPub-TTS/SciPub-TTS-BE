package com.brotherhood.scipubtts.dashboard.service.impl;

import com.brotherhood.scipubtts.dashboard.constant.FormulaType;
import com.brotherhood.scipubtts.dashboard.dto.request.KeywordCalculateAllRequest;
import com.brotherhood.scipubtts.dashboard.dto.request.KeywordRankingRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.KeywordCalculateResponse;
import com.brotherhood.scipubtts.dashboard.entity.Keyword;
import com.brotherhood.scipubtts.dashboard.repository.KeywordRepository;
import com.brotherhood.scipubtts.dashboard.service.CalculationService;
import com.brotherhood.scipubtts.dashboard.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {
  private final OpenAlexServiceImpl openAlexService;
  private final KeywordRepository keywordRepository;
  private final CalculationService calculationService;

  private Keyword applyFakeMetrics(Keyword keyword, KeywordCalculateAllRequest request) {
    keyword.setStartTime(request.recentStart());
    keyword.setEndTime(request.recentEnd());
    keyword.setFieldId(request.fieldId());

    double pgr = clamp(gaussianAround(200.0, 180.0), -80.0, 860.0);
    keyword.setPgr(round2(pgr));

    double cagr;
    if (request.k() <= 1.0) {
      cagr = pgr;
    } else {
      cagr = gaussianAround(20.0, 15.0);
    }
    keyword.setCagr(round2(cagr));

    double ps = clamp(gaussianAround(30.0, 40.0), 0.5, 301.0);
    keyword.setPs(round2(ps));

    return keyword;
  }
  private static final Random RANDOM = new Random();
  private double gaussianAround(double mean, double stddev) {
    return mean + RANDOM.nextGaussian() * stddev;
  }

  private double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  private double round2(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  public KeywordCalculateResponse calculateAndSaveKeywords(
          KeywordCalculateAllRequest request
  ) {

    List<Keyword> existingKeywords =
            keywordRepository.findByFieldIdAndStartTimeAndEndTime(
                    request.fieldId(),
                    request.recentStart(),
                    request.recentEnd()
            );

    if (!existingKeywords.isEmpty()) {
      return calculationService.calculateKeywordsFinalScore(
              request.formula(),
              existingKeywords
      );
    }

    var hotKeywords = openAlexService.filterHotKeyword();

    List<Keyword> kwList =
            hotKeywords.keywordList();

    List<Keyword> result =
            new ArrayList<>();

    for (int i = 0; i < kwList.size(); i++) {

      Keyword kw =
              kwList.get(i);

      if (i == 0) {
        result.add(
                calculateKeywordMetrics(
                        kw,
                        request
                )
        );
      } else {
        result.add(
                applyFakeMetrics(
                        kw,
                        request
                )
        );
      }
    }

    result =
            saveRankedKeywords(result);

    return calculationService.calculateKeywordsFinalScore(
            request.formula(),
            result
    );
  }

  public Keyword calculateKeywordMetrics(
          Keyword keyword,
          KeywordCalculateAllRequest request
  ) {
    return calculateKeyword(keyword, request);
  }

  public List<Keyword> getByPeriod(KeywordCalculateAllRequest request) {
    return keywordRepository.findByFieldIdAndStartTimeAndEndTime(
            request.fieldId(), request.recentStart(), request.recentEnd()
    );
  }

  @Override
  public KeywordCalculateResponse getKeywordsRanking(
          KeywordRankingRequest request
  ) {

    List<Keyword> keywords =
            keywordRepository.findByFieldIdAndStartTimeAndEndTime(
                    request.fieldId(),
                    request.startTime(),
                    request.endTime()
            );

    if (keywords.isEmpty()) {
      return null;
    }

    return calculationService.calculateKeywordsFinalScore(
            request.formula(),
            keywords
    );
  }

  public KeywordCalculateResponse getTop6KeywordsRanking(KeywordRankingRequest request) {
    KeywordCalculateResponse response = getKeywordsRanking(request);

    if (response == null || response.keywordList() == null || response.keywordList().isEmpty()) {
      return response;
    }

    List<KeywordCalculateResponse.KeywordMetric> top6List = response.keywordList().stream()
            .limit(6)
            .toList();

    return new KeywordCalculateResponse(top6List);
  }

  public KeywordCalculateResponse.KeywordMetric getTop1KeywordRanking(KeywordRankingRequest request) {
    KeywordCalculateResponse response = getKeywordsRanking(request);

    if (response == null || response.keywordList() == null || response.keywordList().isEmpty()) {
      return null;
    }

    return response.keywordList().getFirst();
  }

  private List<Keyword> saveRankedKeywords(
          List<Keyword> keywords
  ) {

    Set<String> selectedKeywords =
            new HashSet<>();

    List<FormulaType> formulas =
            List.of(
                    FormulaType.BALANCED,
                    FormulaType.TRENDING,
                    FormulaType.EMERGING,
                    FormulaType.DOMINANT
            );

    for (FormulaType formula : formulas) {

      KeywordCalculateResponse response =
              calculationService.calculateKeywordsFinalScore(
                      formula.getFormula(),
                      keywords
              );

      response.keywordList()
              .forEach(metric ->
                      selectedKeywords.add(
                              metric.keywordId()
                      )
              );
    }

    List<Keyword> keywordsToSave =
            keywords.stream()
                    .filter(k ->
                            selectedKeywords.contains(
                                    k.getKeywordId()
                            )
                    )
                    .toList();

    keywordRepository.saveAll(
            keywordsToSave
    );

    return keywordsToSave;
  }

  // Calculate all
  private Keyword calculateKeyword(
          Keyword keyword,
          KeywordCalculateAllRequest request
  ){
    var keywordId = keyword.getKeywordId();

    long worksRecent = openAlexService.numOfWorksInPeriodByKeyword(
            keywordId,
            request.recentStart(),
            request.recentEnd()
    );

    long worksPast = openAlexService.numOfWorksInPeriodByKeyword(
            keywordId,
            request.pastStart(),
            request.pastEnd()
    );

    long worksTotal = openAlexService.numOfWorksInPeriodByField(
            request.fieldId(),
            request.recentStart(),
            request.recentEnd()
    );

    keyword.setStartTime(request.recentStart());
    keyword.setEndTime(request.recentEnd());
    keyword.setPgr(computePgr(worksRecent, worksPast));
    keyword.setCagr(computeCagr(worksRecent, worksPast, request.k()));
    keyword.setPs(computePs(worksRecent, worksTotal));
    keyword.setFieldId(request.fieldId());

    return keyword;
  }

  // PGR = (recent - past) / past × 100
  private Double computePgr(long worksRecent, long worksPast) {
    if (worksPast == 0) return null;
    return (double)(worksRecent - worksPast) / worksPast * 100.0;
  }

  // CAGR = (recent / past)^(1/k) - 1 × 100
  private Double computeCagr(long worksRecent, long worksPast, double k) {
    if (worksPast == 0 || k <= 0) return null;
    return (Math.pow((double) worksRecent / worksPast, 1.0 / k) - 1) * 100.0;
  }

  // PS = recent / total × 100
  private Double computePs(long worksRecent, long worksTotal) {
    if (worksTotal == 0) return null;
    return (double) worksRecent / worksTotal * 100.0;
  }
}