package zerobase.weather.weatherDiary.service;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.weatherDiary.WeatherDiaryApplication;
import zerobase.weather.weatherDiary.entity.DateWeather;
import zerobase.weather.weatherDiary.entity.Diary;
import zerobase.weather.weatherDiary.repository.DateWeatherRepository;
import zerobase.weather.weatherDiary.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

    @Value("${openWeatherMap.key}")
    private String apiKey;
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherDiaryApplication.class);

    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시마다
    public void saveWeatherDate() {
        logger.info("오늘도 날짜 데이터 잘 가져옴");
        dateWeatherRepository.save(getWeatherFromApi());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        logger.info("started to create diary");
        // 날씨 데이터 가져오기 (db에 이미 저장된 정보 가져오기)
        DateWeather dateWeather = getDateWeather(date);
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text);

        diaryRepository.save(nowDiary);
        logger.info("end to create diary");
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherList = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherList.size() == 0) {
            return getWeatherFromApi();
        }
        return dateWeatherList.get(0);
    }

    private DateWeather getWeatherFromApi() {
        // open weather map 에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();

        // 받아온 날씨 json 파싱하기
        Map<String, Object> parseWeather = parseWeather(weatherData);

        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now())
                .setWeather(parseWeather.get("main").toString())
                .setIcon(parseWeather.get("icon").toString())
                .setTemperature((Double) parseWeather.get("temp"));

        return dateWeather;
    }

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            BufferedReader br;

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();

        } catch (Exception e) {
            return "failed to get response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> resultMap = new HashMap<>();
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));
        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        return resultMap;
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.debug("start read diary");
/*
        if (date.isAfter(LocalDate.ofYearDay(3050, 1))) {
            throw new InvalidDate();
        }
*/
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
    }

    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
