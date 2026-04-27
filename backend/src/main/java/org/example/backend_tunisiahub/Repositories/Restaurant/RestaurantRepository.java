package org.example.backend_tunisiahub.Repositories.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByCuisine(Cuisine cuisine);

    List<Restaurant> findByCuisineAndPriceRangeAndRatingGreaterThanEqualOrderByRatingDescIdDesc(
            Cuisine cuisine,
            String priceRange,
            Double rating
    );

    List<Restaurant> findByCuisineAndRatingGreaterThanEqualOrderByRatingDescIdDesc(
            Cuisine cuisine,
            Double rating
    );

    List<Restaurant> findByPriceRangeAndRatingGreaterThanEqualOrderByRatingDescIdDesc(
            String priceRange,
            Double rating
    );

    List<Restaurant> findByRatingGreaterThanEqualOrderByRatingDescIdDesc(Double rating);

    @Query("select distinct r.cuisine from Restaurant r where r.cuisine is not null order by r.cuisine")
    List<Cuisine> findDistinctCuisines();

    @Query("""
            select r from Restaurant r
            where (:cuisine is null or r.cuisine = :cuisine)
              and (
                    :query = ''
                 or lower(coalesce(r.name, '')) like lower(concat('%', :query, '%'))
                 or lower(coalesce(r.address, '')) like lower(concat('%', :query, '%'))
                 or :city = ''
                 or lower(coalesce(r.address, '')) like lower(concat('%', :city, '%'))
              )
            order by r.id desc
            """)
    List<Restaurant> prefilterAiCandidates(@Param("query") String query,
                                           @Param("city") String city,
                                           @Param("cuisine") Cuisine cuisine,
                                           Pageable pageable);

    @Query("""
            select r from Restaurant r
            where (:cuisine is null or r.cuisine = :cuisine)
              and (:priceRange is null or upper(coalesce(r.priceRange, '')) = :priceRange)
              and coalesce(r.rating, 0) >= :minRating
            order by coalesce(r.rating, 0) desc, r.id desc
            """)
    List<Restaurant> findRecommendationCandidates(@Param("cuisine") Cuisine cuisine,
                                                  @Param("priceRange") String priceRange,
                                                  @Param("minRating") double minRating,
                                                  Pageable pageable);

    @Query("""
            select r from Restaurant r
            where (:cuisine is null or r.cuisine = :cuisine)
              and (:priceRange is null or upper(coalesce(r.priceRange, '')) = :priceRange)
              and coalesce(r.rating, 0) >= :minRating
              and r.id not in :excludedIds
            order by coalesce(r.rating, 0) desc, r.id desc
            """)
    List<Restaurant> findRecommendationCandidatesExcluding(@Param("cuisine") Cuisine cuisine,
                                                           @Param("priceRange") String priceRange,
                                                           @Param("minRating") double minRating,
                                                           @Param("excludedIds") Set<Long> excludedIds,
                                                           Pageable pageable);

    @Query("""
            select r from Restaurant r
            where coalesce(r.rating, 0) >= :minRating
            order by coalesce(r.rating, 0) desc, r.id desc
            """)
    List<Restaurant> findTopRatedRestaurants(@Param("minRating") double minRating, Pageable pageable);

    @Query("""
            select r from Restaurant r
            where coalesce(r.rating, 0) >= :minRating
              and r.id not in :excludedIds
            order by coalesce(r.rating, 0) desc, r.id desc
            """)
    List<Restaurant> findTopRatedRestaurantsExcluding(@Param("minRating") double minRating,
                                                      @Param("excludedIds") Set<Long> excludedIds,
                                                      Pageable pageable);
}
