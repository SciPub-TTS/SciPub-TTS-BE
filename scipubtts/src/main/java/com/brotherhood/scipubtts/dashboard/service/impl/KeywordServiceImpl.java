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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {
  private final OpenAlexServiceImpl openAlexService;
  private final KeywordRepository keywordRepository;
  private final CalculationService calculationService;

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
      return new KeywordCalculateResponse(existingKeywords);
    }

    var hotKeywords = openAlexService.filterHotKeyword();

    List<Keyword> result = new ArrayList<>();

    for (Keyword kw : hotKeywords.keywordList()) {
      result.add(
              calculateKeywordMetrics(
                      kw,
                      request
              )
      );
    }

    result = saveRankedKeywords(result);

    return new KeywordCalculateResponse(result);
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

  private List<Keyword> saveRankedKeywords(List<Keyword> keywords) {
    Set<String> selectedKeywords = new HashSet<>();
    List<FormulaType> formulas = List.of(
            FormulaType.BALANCED,
            FormulaType.TRENDING,
            FormulaType.EMERGING,
            FormulaType.DOMINANT
    );

    for (FormulaType formula : formulas) {
      KeywordCalculateResponse response = calculationService.calculateKeywordsFinalScore(formula.getFormula(), keywords);

      for (Keyword keyword : response.keywordList()) {
        selectedKeywords.add(keyword.getKeywordId());
      }
    }

    List<Keyword> keywordsToSave = keywords.stream()
            .filter(keyword -> selectedKeywords.contains(keyword.getKeywordId()))
            .toList();

    keywordRepository.saveAll(keywordsToSave);
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