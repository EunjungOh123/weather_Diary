package zerobase.weather.weatherDiary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.weatherDiary.entity.Diary;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByDate (LocalDate date);
    List<Diary> findAllByDateBetween (LocalDate startDate, LocalDate endDate);
    Diary getFirstByDate(LocalDate date);
    @Transactional
    void deleteAllByDate(LocalDate date);
}
