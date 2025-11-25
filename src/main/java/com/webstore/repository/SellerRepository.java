package com.webstore.repository;

import com.webstore.entity.Seller;
import com.webstore.entity.Seller.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Integer> {

    /*SELECT * FROM sellers WHERE eamil= ?
     * Find seller by email
     */
     Optional<Seller> findByEmail(String email);

     /*checks if the email already exists */
     boolean existsByEmail(String email);

     /*Finds all sellers by its status */
     List<Seller> findByStatus(SellerStatus status);

     /*finds all sellers by name */
     List<Seller> findByNameContainingIgnoreCase(String name);

     /*Finds seller by joining date */
     List<Seller> findByJoiningDateAfter(LocalDate joiningDate);
     
     /*Find sellers by joining date range*/
     List<Seller> findByJoiningDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Seller s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Seller> searchSellers(@Param("keyword") String keyword);

    /* Count active sellers*/
    long countByStatus(SellerStatus status);

}
