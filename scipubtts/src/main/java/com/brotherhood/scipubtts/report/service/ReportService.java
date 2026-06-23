package com.brotherhood.scipubtts.report.service;

import com.brotherhood.scipubtts.report.dto.ReportOverviewResponse;
import com.brotherhood.scipubtts.report.dto.TopicFormulaComparisonResponse;
import com.brotherhood.scipubtts.report.dto.TopicTrendReportResponse;

public interface ReportService {

  ReportOverviewResponse getOverview(String startTime, String endTime, String fieldId);

  TopicFormulaComparisonResponse compareTopicAcrossFormulas(
          String topicId, String fieldId, String startTime, String endTime);

  TopicTrendReportResponse getTopicTrendReport(
          String topicId, String fieldId, String startTime, String endTime);
}