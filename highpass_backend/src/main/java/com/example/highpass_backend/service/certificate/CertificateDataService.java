package com.example.highpass_backend.service.certificate;

import com.example.highpass_backend.entity.certificate.DataIndustryCertificateSchedule;
import com.example.highpass_backend.entity.certificate.NationalCertificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateDataService {

    private static final String FIELD_SEQUENCE_NO = "순번";
    private static final String FIELD_EXAM_NAME = "시험명";
    private static final String FIELD_EXAM_CATEGORY = "시험구분";
    private static final String FIELD_EXAM_ROUND = "회차";
    private static final String FIELD_EXAM_DATE = "시험일";
    private static final String FIELD_EXAM_START_TIME = "시험시작시간";
    private static final String FIELD_APPLY_START_DATE = "접수시작일";
    private static final String FIELD_APPLY_END_DATE = "접수마감일";
    private static final String FIELD_EXAM_PLACE = "시험장소";
    private static final String FIELD_RESULT_ANNOUNCEMENT_DATE = "합격자발표일";
    private static final String FIELD_EXAM_TYPE = "시험유형";

    private static final String ENGINEER_URL = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getEList?serviceKey=";
    private static final String CRAFTSMAN_URL = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getCList?serviceKey=";
    private static final String DATA_INDUSTRY_URL = "https://api.odcloud.kr/api/15062838/v1/uddi:203e0beb-5aa5-448d-a167-c6e3f0cf2f4d";
    private static final int DATA_INDUSTRY_PAGE_SIZE = 100;

    @Value("${api.public-data.key}")
    private String apiServiceKey;

    public List<NationalCertificate> fetchAll() {
        List<NationalCertificate> result = new ArrayList<>();
        result.addAll(fetchEntitiesWithRetry(ENGINEER_URL));
        result.addAll(fetchEntitiesWithRetry(CRAFTSMAN_URL));
        return dedupe(result);
    }

    public JSONObject fetchDataIndustrySchedulePreview(int page, int perPage, int examYear) {
        JSONObject payload = requestDataIndustryPayload(page, perPage);
        if (payload == null) {
            return new JSONObject()
                    .put("page", page)
                    .put("perPage", perPage)
                    .put("examYear", examYear)
                    .put("data", new JSONArray())
                    .put("message", "데이터산업진흥원 일정 조회에 실패했습니다.");
        }

        JSONArray filtered = filterRowsByExamYear(payload.optJSONArray("data"), examYear);
        payload.put("data", filtered);
        payload.put("currentCount", filtered.length());
        payload.put("matchCount", filtered.length());
        payload.put("totalCount", filtered.length());
        payload.put("page", page);
        payload.put("perPage", perPage);
        payload.put("examYear", examYear);
        return payload;
    }

    public List<DataIndustryCertificateSchedule> fetchDataIndustrySchedulesByYear(int examYear) {
        List<DataIndustryCertificateSchedule> result = new ArrayList<>();
        int page = 1;

        while (true) {
            JSONObject payload = requestDataIndustryPayload(page, DATA_INDUSTRY_PAGE_SIZE);
            if (payload == null) {
                break;
            }

            JSONArray pageData = payload.optJSONArray("data");
            if (pageData == null || pageData.isEmpty()) {
                break;
            }

            JSONArray filtered = filterRowsByExamYear(pageData, examYear);
            for (int i = 0; i < filtered.length(); i++) {
                Object item = filtered.opt(i);
                if (!(item instanceof JSONObject row)) {
                    continue;
                }

                String examName = optString(row, FIELD_EXAM_NAME);
                LocalDate examDate = parseDate(optString(row, FIELD_EXAM_DATE));
                if (examName == null || examName.isBlank() || examDate == null) {
                    continue;
                }

                result.add(DataIndustryCertificateSchedule.builder()
                        .examYear(examYear)
                        .sequenceNo(parseInteger(optString(row, FIELD_SEQUENCE_NO)))
                        .examName(examName)
                        .examCategory(optString(row, FIELD_EXAM_CATEGORY))
                        .examRound(parseInteger(optString(row, FIELD_EXAM_ROUND)))
                        .examDate(examDate)
                        .examStartTime(optString(row, FIELD_EXAM_START_TIME))
                        .applyStartDate(parseDate(optString(row, FIELD_APPLY_START_DATE)))
                        .applyEndDate(parseDate(optString(row, FIELD_APPLY_END_DATE)))
                        .examPlace(optString(row, FIELD_EXAM_PLACE))
                        .resultAnnouncementDate(parseDate(optString(row, FIELD_RESULT_ANNOUNCEMENT_DATE)))
                        .examType(optString(row, FIELD_EXAM_TYPE))
                        .build());
            }

            if (pageData.length() < DATA_INDUSTRY_PAGE_SIZE) {
                break;
            }

            page++;
        }

        return dedupeDataIndustry(result);
    }

    private JSONObject requestDataIndustryPayload(int page, int perPage) {
        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder.fromUriString(DATA_INDUSTRY_URL)
                .queryParam("serviceKey", apiServiceKey)
                .queryParam("page", Math.max(page, 1))
                .queryParam("perPage", Math.max(perPage, 1))
                .queryParam("returnType", "JSON")
                .build(false)
                .toUriString();

        try {
            String response = restTemplate.getForObject(new URI(url), String.class);
            if (response == null || response.isBlank()) {
                return new JSONObject()
                        .put("page", page)
                        .put("perPage", perPage)
                        .put("data", new JSONArray())
                        .put("message", "데이터산업진흥원 일정 응답이 비어 있습니다.");
            }

            String trimmed = response.trim();
            JSONObject payload = trimmed.startsWith("<")
                    ? XML.toJSONObject(trimmed)
                    : new JSONObject(trimmed);
            payload.put("page", page);
            payload.put("perPage", perPage);
            return payload;
        } catch (Exception exception) {
            log.error("데이터산업진흥원 자격검정 일정 조회 중 오류", exception);
            return null;
        }
    }

    private JSONArray filterRowsByExamYear(JSONArray source, int examYear) {
        if (source == null) {
            return new JSONArray();
        }

        JSONArray filtered = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            Object item = source.opt(i);
            if (!(item instanceof JSONObject row)) {
                continue;
            }

            String examDate = optString(row, FIELD_EXAM_DATE);
            if (hasYear(examDate, examYear)) {
                filtered.put(row);
            }
        }

        return filtered;
    }

    private boolean hasYear(String value, int examYear) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.trim().startsWith(String.valueOf(examYear));
    }

    private List<NationalCertificate> fetchEntities(String baseUrl) {
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + apiServiceKey;

        try {
            String response = restTemplate.getForObject(new URI(url), String.class);
            if (response == null || response.isBlank()) {
                return Collections.emptyList();
            }

            JSONObject json = response.trim().startsWith("<") ? XML.toJSONObject(response) : new JSONObject(response);
            JSONObject responseObj = json.optJSONObject("response");
            if (responseObj == null) {
                return Collections.emptyList();
            }

            JSONObject body = responseObj.optJSONObject("body");
            if (body == null) {
                return Collections.emptyList();
            }

            JSONObject items = body.optJSONObject("items");
            if (items == null) {
                return Collections.emptyList();
            }

            Object item = items.opt("item");
            JSONArray itemArray;
            if (item instanceof JSONArray jsonArray) {
                itemArray = jsonArray;
            } else if (item instanceof JSONObject jsonObject) {
                itemArray = new JSONArray();
                itemArray.put(jsonObject);
            } else {
                return Collections.emptyList();
            }

            List<NationalCertificate> result = new ArrayList<>();
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject element = itemArray.getJSONObject(i);
                LocalDate writtenApplyStart = parseDate(optString(element, "docregstartdt"));
                LocalDate writtenApplyEnd = parseDate(optString(element, "docregenddt"));
                LocalDate writtenExamDate = parseDate(optString(element, "docexamdt"));
                LocalDate writtenResultDate = parseDate(optString(element, "docpassdt"));
                LocalDate practicalApplyStart = parseDate(optString(element, "pracregstartdt"));
                LocalDate practicalApplyEnd = parseDate(optString(element, "pracregenddt"));
                LocalDate practicalExamDate = parseDate(optString(element, "pracexamstartdt"));
                LocalDate practicalResultDate = parseDate(optString(element, "pracpassdt"));

                result.add(NationalCertificate.builder()
                        .certificateName(optString(element, "description"))
                        .year(extractYear(element, writtenApplyStart, writtenExamDate, practicalExamDate))
                        .writtenApplyStart(writtenApplyStart)
                        .writtenApplyEnd(writtenApplyEnd)
                        .writtenExamDate(writtenExamDate)
                        .writtenResultDate(writtenResultDate)
                        .practicalApplyStart(practicalApplyStart)
                        .practicalApplyEnd(practicalApplyEnd)
                        .practicalExamDate(practicalExamDate)
                        .practicalResultDate(practicalResultDate)
                        .build());
            }

            return result;
        } catch (Exception exception) {
            log.error("Q-Net 자격증 일정 수집 중 오류", exception);
            return Collections.emptyList();
        }
    }

    private List<NationalCertificate> fetchEntitiesWithRetry(String baseUrl) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            List<NationalCertificate> result = fetchEntities(baseUrl);
            if (!result.isEmpty()) {
                return result;
            }
            log.warn("Q-Net API retry {}/{}", i + 1, maxRetry);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private List<NationalCertificate> dedupe(List<NationalCertificate> source) {
        Map<String, NationalCertificate> map = new LinkedHashMap<>();
        for (NationalCertificate certificate : source) {
            String name = certificate.getCertificateName();
            if (name == null || name.isBlank()) {
                continue;
            }
            String key = buildDedupeKey(
                    certificate.getCertificateName(),
                    certificate.getWrittenApplyStart(),
                    certificate.getPracticalApplyStart()
            );
            map.put(key, certificate);
        }
        return new ArrayList<>(map.values());
    }

    private List<DataIndustryCertificateSchedule> dedupeDataIndustry(List<DataIndustryCertificateSchedule> source) {
        Map<String, DataIndustryCertificateSchedule> map = new LinkedHashMap<>();
        for (DataIndustryCertificateSchedule schedule : source) {
            String name = schedule.getExamName();
            LocalDate examDate = schedule.getExamDate();
            if (name == null || name.isBlank() || examDate == null) {
                continue;
            }

            String key = normalize(name)
                    + "|" + normalize(schedule.getExamType())
                    + "|" + (schedule.getExamRound() == null ? "" : schedule.getExamRound())
                    + "|" + examDate;
            map.put(key, schedule);
        }
        return new ArrayList<>(map.values());
    }

    private String buildDedupeKey(String certificateName, LocalDate writtenApplyStart, LocalDate practicalApplyStart) {
        return normalize(certificateName) + "|" + normalizeDate(writtenApplyStart) + "|" + normalizeDate(practicalApplyStart);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private String optString(JSONObject element, String key) {
        String value = element.optString(key, null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int extractYear(JSONObject element, LocalDate... fallbackDates) {
        for (String key : List.of("implYy", "implyy", "year", "examYear")) {
            String value = optString(element, key);
            if (value != null) {
                String digits = value.replaceAll("[^0-9]", "");
                if (digits.length() >= 4) {
                    return Integer.parseInt(digits.substring(0, 4));
                }
            }
        }

        for (LocalDate date : fallbackDates) {
            if (date != null) {
                return date.getYear();
            }
        }

        return LocalDate.now().getYear();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            String cleanDate = dateStr.replaceAll("[^0-9]", "");
            if (cleanDate.length() < 8) {
                return null;
            }
            return LocalDate.parse(cleanDate.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception exception) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String digits = value.replaceAll("[^0-9-]", "");
            if (digits.isBlank()) {
                return null;
            }
            return Integer.parseInt(digits);
        } catch (Exception exception) {
            return null;
        }
    }
}
