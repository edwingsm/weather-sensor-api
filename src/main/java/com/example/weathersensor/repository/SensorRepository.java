package com.example.weathersensor.repository;

import com.example.weathersensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    @Query("SELECT s FROM Sensor s WHERE s.location = :location")
    List<Sensor> findByLocation(@Param("location") String location);

    @Query("SELECT s FROM Sensor s WHERE s.tag = :tag")
    Optional<Sensor> findByTag(@Param("tag") String tag);

    boolean existsByTag(String tag);
}