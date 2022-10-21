package zerobase.weather.weatherDiary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.weather.weatherDiary.entity.DateWeather;

import java.time.LocalDate;
import java.util.List;

public interface DateWeatherRepository extends JpaRepository<DateWeather, Long> {
    List<DateWeather> findAllByDate(LocalDate date);
}
