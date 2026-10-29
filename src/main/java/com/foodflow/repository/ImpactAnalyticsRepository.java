package com.foodflow.repository;

import com.foodflow.document.ImpactAnalyticsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImpactAnalyticsRepository extends MongoRepository<ImpactAnalyticsDocument, String> {

    Optional<ImpactAnalyticsDocument> findByDate(String date);

    List<ImpactAnalyticsDocument> findByYearAndMonthOrderByDateAsc(Integer year, Integer month);

    List<ImpactAnalyticsDocument> findByYearAndWeekOrderByDateAsc(Integer year, Integer week);

    List<ImpactAnalyticsDocument> findByYearOrderByDateAsc(Integer year);

    List<ImpactAnalyticsDocument> findAllByOrderByDateAsc();
}
